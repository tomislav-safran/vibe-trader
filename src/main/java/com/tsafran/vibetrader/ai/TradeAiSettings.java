package com.tsafran.vibetrader.ai;

import com.tsafran.vibetrader.exchange.ExchangeInterval;

public record TradeAiSettings(
        String strategy,
        int candleLookbackLimit,
        ExchangeInterval candleLookbackInterval,
        int certaintyThreshold
) {
}
