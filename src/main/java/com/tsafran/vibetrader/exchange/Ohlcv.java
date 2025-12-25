package com.tsafran.vibetrader.exchange;

import java.math.BigDecimal;

public record Ohlcv(
        long startTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume,
        BigDecimal turnover
) {
}
