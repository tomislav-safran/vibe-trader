package com.tsafran.vibetrader.exchange.bybit;

import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.domain.market.response.kline.MarketKlineResult;
import com.bybit.api.client.domain.position.TpslMode;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.ExchangeMarketMapper;
import com.tsafran.vibetrader.exchange.ExchangeOrderSide;
import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.exchange.Ohlcv;
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
    private final BybitApiTradeRestClient tradeClient;
    private final ExchangeMarketMapper<CategoryType, MarketInterval> marketMapper;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public List<Ohlcv> getKlines(String symbol, ExchangeCategory category, ExchangeInterval interval, @Min(1) int limit) {
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(interval, "interval");

        MarketDataRequest request = MarketDataRequest.builder()
                .category(marketMapper.mapCategory(category))
                .symbol(symbol)
                .marketInterval(marketMapper.mapInterval(interval))
                .limit(limit)
                .build();

        Object response = marketClient.getMarketLinesData(request);
        List<MarketKlineEntry> entries = getMarketKlineEntries(response);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .map(entry -> new Ohlcv(
                        entry.getStartTime(),
                        parseDecimal(entry.getOpenPrice()),
                        parseDecimal(entry.getHighPrice()),
                        parseDecimal(entry.getLowPrice()),
                        parseDecimal(entry.getClosePrice()),
                        parseDecimal(entry.getVolume()),
                        parseDecimal(entry.getTurnover())
                ))
                .toList();
    }

    @Override
    public String placeFuturesMarketOrder(FuturesMarketOrderRequest request) {
        Objects.requireNonNull(request, "request");

        TradeOrderRequest.TradeOrderRequestBuilder builder = TradeOrderRequest.builder()
                .category(marketMapper.mapCategory(request.category()))
                .symbol(request.symbol())
                .side(mapSide(request.side()))
                .orderType(TradeOrderType.MARKET)
                .tpslMode(TpslMode.FULL.name())
                .isLeverage(1)
                .qty(request.quantity().toPlainString());

        if (request.takeProfit() != null) {
            builder.takeProfit(request.takeProfit().toPlainString());
        }
        if (request.stopLoss() != null) {
            builder.stopLoss(request.stopLoss().toPlainString());
        }

        Object response = tradeClient.createOrder(builder.build());
        OrderResponse orderResponse = getOrderResponse(response);
        return orderResponse == null ? null : orderResponse.getOrderId();
    }

    private static List<MarketKlineEntry> getMarketKlineEntries(Object response) {
        GenericResponse<MarketKlineResult> genericResponse = MAPPER.convertValue(
                response,
                new TypeReference<>() {}
        );

        if (genericResponse.getRetCode() != 0) {
            throw new IllegalStateException(
                    "Bybit error: " + genericResponse.getRetCode() + " " + genericResponse.getRetMsg()
            );
        }

        MarketKlineResult result = genericResponse.getResult();
        if (result == null || result.getMarketKlineEntries() == null) {
            return List.of();
        }

        return result.getMarketKlineEntries();
    }

    private static OrderResponse getOrderResponse(Object response) {
        GenericResponse<OrderResponse> genericResponse = MAPPER.convertValue(
                response,
                new TypeReference<>() {}
        );

        if (genericResponse.getRetCode() != 0) {
            throw new IllegalStateException(
                    "Bybit error: " + genericResponse.getRetCode() + " " + genericResponse.getRetMsg()
            );
        }

        return genericResponse.getResult();
    }

    private static Side mapSide(ExchangeOrderSide side) {
        return switch (side) {
            case LONG -> Side.BUY;
            case SHORT -> Side.SELL;
        };
    }

    private static BigDecimal parseDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
