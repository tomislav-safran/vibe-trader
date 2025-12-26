package com.tsafran.vibetrader.shell;

import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.position.PositionService;
import com.tsafran.vibetrader.position.ProposedPosition;
import com.tsafran.vibetrader.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Command(command = "position", description = "Position sizing commands")
public class PositionCommands {
    private final PositionService positionService;

    @Command(command = "size", description = "Calculate a risk-based futures position size")
    public String size(
            @Option(longNames = "symbol") String symbol,
            @Option(longNames = "side") String side,
            @Option(longNames = "entry") BigDecimal entryPrice,
            @Option(longNames = "tp") BigDecimal takeProfitPrice,
            @Option(longNames = "sl") BigDecimal stopLossPrice
    ) {
        var orderSide = Util.parseOrderSide(side);
        ProposedPosition proposedPosition = new ProposedPosition(
                symbol,
                orderSide,
                entryPrice,
                takeProfitPrice,
                stopLossPrice
        );

        FuturesMarketOrderRequest orderRequest = positionService.buildMarketOrder(proposedPosition);
        return "Symbol: " + orderRequest.symbol()
                + ", Side: " + orderRequest.side()
                + ", Qty: " + orderRequest.quantity()
                + ", TP: " + orderRequest.takeProfit()
                + ", SL: " + orderRequest.stopLoss();
    }

}
