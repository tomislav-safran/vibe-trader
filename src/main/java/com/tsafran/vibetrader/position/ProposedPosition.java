package com.tsafran.vibetrader.position;

import com.tsafran.vibetrader.exchange.ExchangeOrderSide;

import java.math.BigDecimal;

public record ProposedPosition(
        String symbol,
        ExchangeOrderSide side,
        BigDecimal entryPrice,
        BigDecimal takeProfitPrice,
        BigDecimal stopLossPrice
) {
    public ProposedPosition {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must be provided");
        }
        if (side == null) {
            throw new IllegalArgumentException("side must be provided");
        }
        if (entryPrice == null || entryPrice.signum() <= 0) {
            throw new IllegalArgumentException("entryPrice must be positive");
        }
        if (takeProfitPrice == null || takeProfitPrice.signum() <= 0) {
            throw new IllegalArgumentException("takeProfitPrice must be positive");
        }
        if (stopLossPrice == null || stopLossPrice.signum() <= 0) {
            throw new IllegalArgumentException("stopLossPrice must be positive");
        }
    }
}
