package com.tsafran.vibetrader.exchange.bybit;

import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.domain.position.TpslMode;
import com.bybit.api.client.domain.trade.TimeInForce;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.restApi.BybitApiAccountRestClient;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.ExchangeOrderSide;
import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.exchange.InstrumentPrecision;
import com.tsafran.vibetrader.util.Util;
import com.tsafran.vibetrader.exchange.Ohlcv;
import com.tsafran.vibetrader.exchange.WalletBalanceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BybitExchange implements Exchange {
    private final BybitApiMarketRestClient marketClient;
    private final BybitApiAccountRestClient accountClient;
    private final BybitApiTradeRestClient tradeClient;

    @Override
    public List<Ohlcv> getKlines(String symbol, ExchangeCategory category, ExchangeInterval interval, @Min(1) int limit) {
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(interval, "interval");

        MarketDataRequest request = MarketDataRequest.builder()
                .category(BybitUtil.mapCategory(category))
                .symbol(symbol)
                .marketInterval(BybitUtil.mapInterval(interval))
                .limit(limit)
                .build();

        Object response = marketClient.getMarketLinesData(request);
        List<MarketKlineEntry> entries = BybitUtil.getMarketKlineEntries(response);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .map(entry -> new Ohlcv(
                        entry.getStartTime(),
                        Util.parseDecimal(entry.getOpenPrice()),
                        Util.parseDecimal(entry.getHighPrice()),
                        Util.parseDecimal(entry.getLowPrice()),
                        Util.parseDecimal(entry.getClosePrice()),
                        Util.parseDecimal(entry.getVolume()),
                        Util.parseDecimal(entry.getTurnover())
                ))
                .toList();
    }

    @Override
    public String placeFuturesMarketOrder(FuturesMarketOrderRequest request) {
        Objects.requireNonNull(request, "request");

        TradeOrderRequest.TradeOrderRequestBuilder builder = TradeOrderRequest.builder()
                .category(BybitUtil.mapCategory(request.category()))
                .symbol(request.symbol())
                .side(BybitUtil.mapSide(request.side()))
                .orderType(TradeOrderType.MARKET)
                .tpslMode(TpslMode.FULL.name())
                .isLeverage(1)
                .closeOnTrigger(true)
                .qty(request.quantity().toPlainString());

        if (request.stopLoss() != null) {
            builder.stopLoss(request.stopLoss().toPlainString());
        }

        Object response = tradeClient.createOrder(builder.build());
        OrderResponse orderResponse = BybitUtil.getOrderResponse(response);
        if (request.takeProfit() != null) {
            placeReduceOnlyTakeProfitOrder(request);
        }
        return orderResponse == null ? null : orderResponse.getOrderId();
    }

    @Override
    public BigDecimal getWalletBalance(WalletBalanceRequest request) {
        Objects.requireNonNull(request, "request");

        AccountDataRequest.AccountDataRequestBuilder builder = AccountDataRequest.builder()
                .accountType(BybitUtil.mapAccountType(request.accountType()));

        Object response = accountClient.getWalletBalance(builder.build());
        return BybitUtil.extractTotalAvailableBalance(response);
    }

    @Override
    public InstrumentPrecision getInstrumentPrecision(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must be provided");
        }

        MarketDataRequest request = MarketDataRequest.builder()
                .category(BybitUtil.mapCategory(ExchangeCategory.LINEAR))
                .symbol(symbol)
                .build();

        Object response = marketClient.getInstrumentsInfo(request);
        return BybitUtil.extractInstrumentPrecision(response, symbol);
    }

    @Override
    public boolean hasOpenOrders(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must be provided");
        }

        TradeOrderRequest request = TradeOrderRequest.builder()
                .category(BybitUtil.mapCategory(ExchangeCategory.LINEAR))
                .symbol(symbol)
                .openOnly(0)
                .build();

        Object response = tradeClient.getOpenOrders(request);
        return BybitUtil.hasOpenOrders(response);
    }

    private void placeReduceOnlyTakeProfitOrder(FuturesMarketOrderRequest request) {
        ExchangeOrderSide tpSide = request.side() == ExchangeOrderSide.LONG
                ? ExchangeOrderSide.SHORT
                : ExchangeOrderSide.LONG;

        TradeOrderRequest tpRequest = TradeOrderRequest.builder()
                .category(BybitUtil.mapCategory(request.category()))
                .symbol(request.symbol())
                .side(BybitUtil.mapSide(tpSide))
                .orderType(TradeOrderType.LIMIT)
                .timeInForce(TimeInForce.POST_ONLY)
                .reduceOnly(true)
                .isLeverage(1)
                .qty(request.quantity().toPlainString())
                .price(request.takeProfit().toPlainString())
                .build();

        tradeClient.createOrder(tpRequest);
    }
}
