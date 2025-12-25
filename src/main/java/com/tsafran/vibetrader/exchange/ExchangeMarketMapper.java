package com.tsafran.vibetrader.exchange;

public interface ExchangeMarketMapper<C, I> {
    C mapCategory(ExchangeCategory category);

    I mapInterval(ExchangeInterval interval);
}
