package com.tsafran.vibetrader.exchange;

import java.math.BigDecimal;

public record InstrumentPrecision(
        BigDecimal basePrecision,
        BigDecimal tickSize
) {
    public InstrumentPrecision {
        if (basePrecision == null || basePrecision.signum() <= 0) {
            throw new IllegalArgumentException("basePrecision must be positive");
        }
        if (tickSize == null || tickSize.signum() <= 0) {
            throw new IllegalArgumentException("tickSize must be positive");
        }
    }
}
