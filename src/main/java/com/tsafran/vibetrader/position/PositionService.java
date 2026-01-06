package com.tsafran.vibetrader.position;

import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.exchange.InstrumentPrecision;
import com.tsafran.vibetrader.exchange.WalletBalanceRequest;
import com.tsafran.vibetrader.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PositionService {
    private static final BigDecimal MAX_RISK_FRACTION = new BigDecimal("0.01");
    private static final int RISK_DIVIDE_SCALE = 16;

    // Bybit fees (as decimals)
    private static final BigDecimal TAKER_FEE = new BigDecimal("0.00055"); // 0.0550%
    private static final BigDecimal MAKER_FEE = new BigDecimal("0.00020"); // 0.0200%

    private final Exchange exchange;

    public FuturesMarketOrderRequest buildMarketOrder(ProposedPosition proposedPosition) {
        Objects.requireNonNull(proposedPosition, "proposedPosition");

        InstrumentPrecision precision = exchange.getInstrumentPrecision(proposedPosition.symbol());
        if (precision == null) {
            throw new IllegalStateException("No instrument precision returned for " + proposedPosition.symbol());
        }

        BigDecimal tickSize = precision.tickSize();
        BigDecimal qtyStep = precision.basePrecision();

        BigDecimal entry = Util.roundToStep(proposedPosition.entryPrice(), tickSize);
        BigDecimal stopLoss = Util.roundToStep(proposedPosition.stopLossPrice(), tickSize);
        BigDecimal takeProfit = Util.roundToStep(proposedPosition.takeProfitPrice(), tickSize);

        BigDecimal priceRiskPerUnit = entry.subtract(stopLoss).abs();
        if (priceRiskPerUnit.signum() == 0) {
            throw new IllegalArgumentException("entryPrice and stopLossPrice must differ");
        }

        BigDecimal balance = exchange.getWalletBalance(new WalletBalanceRequest(null));
        if (balance == null || balance.signum() <= 0) {
            throw new IllegalStateException("No available wallet balance returned");
        }

        BigDecimal riskAmount = balance.multiply(MAX_RISK_FRACTION);

        // Market entry => taker fee on entry
        BigDecimal entryFeePerUnit = entry.multiply(TAKER_FEE);

        BigDecimal riskPerUnit = priceRiskPerUnit.add(entryFeePerUnit);

        BigDecimal rawQty = riskAmount.divide(riskPerUnit, RISK_DIVIDE_SCALE, RoundingMode.DOWN);
        BigDecimal qty = Util.roundDownToStep(rawQty, qtyStep);

        // Cap position size so total value doesn't exceed available balance
        BigDecimal positionValue = qty.multiply(entry);
        if (positionValue.compareTo(balance) > 0) {
            BigDecimal maxQty = balance.divide(entry, RISK_DIVIDE_SCALE, RoundingMode.DOWN);
            qty = Util.roundDownToStep(maxQty, qtyStep);
        }

        if (qty.signum() <= 0) {
            throw new IllegalStateException("Calculated quantity is too small for the instrument precision");
        }

        return new FuturesMarketOrderRequest(
                proposedPosition.symbol(),
                ExchangeCategory.LINEAR,
                proposedPosition.side(),
                qty,
                takeProfit,
                stopLoss
        );
    }
}
