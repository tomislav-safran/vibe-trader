package com.tsafran.vibetrader.indicators;

public record IndicatorConfig(
        IndicatorType type,
        Integer period,
        Integer fastPeriod,
        Integer slowPeriod,
        Integer signalPeriod
) {
}
