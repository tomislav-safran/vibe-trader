package com.tsafran.vibetrader.exchange;

import java.math.BigDecimal;
import java.util.Objects;

public record FuturesMarketOrderRequest(
        String symbol,
        ExchangeCategory category,
        ExchangeOrderSide side,
        BigDecimal quantity,
        BigDecimal takeProfit,
        BigDecimal stopLoss
) {
    public FuturesMarketOrderRequest {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must be provided");
        }

        Objects.requireNonNull(side, "side must be provided");
        Objects.requireNonNull(quantity, "quantity must be provided");

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        if (takeProfit != null && takeProfit.signum() <= 0) {
            throw new IllegalArgumentException("takeProfit must be positive when provided");
        }
        if (stopLoss != null && stopLoss.signum() <= 0) {
            throw new IllegalArgumentException("stopLoss must be positive when provided");
        }
    }
}
