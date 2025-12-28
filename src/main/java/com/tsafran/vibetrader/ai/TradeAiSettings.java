package com.tsafran.vibetrader.ai;

import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.indicators.IndicatorConfig;

import java.util.List;

public record TradeAiSettings(
        String strategy,
        int candleLookbackLimit,
        int indicatorLookbackLimit,
        ExchangeInterval candleLookbackInterval,
        int certaintyThreshold,
        List<IndicatorConfig> indicators
) {
}
