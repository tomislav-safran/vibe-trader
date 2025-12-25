package com.tsafran.vibetrader.exchange;

import java.util.List;

public interface Exchange {

    List<Ohlcv> getKlines(String symbol, ExchangeCategory category, ExchangeInterval interval, int limit);

    String placeFuturesMarketOrder(FuturesMarketOrderRequest request);

    java.math.BigDecimal getWalletBalance(WalletBalanceRequest request);

    InstrumentPrecision getInstrumentPrecision(String symbol);
}
