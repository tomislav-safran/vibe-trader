package com.tsafran.vibetrader.exchange.bybit;

import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.market.response.kline.MarketKlineEntry;
import com.bybit.api.client.domain.market.response.kline.MarketKlineResult;
import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentEntry;
import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentInfoResult;
import com.bybit.api.client.domain.trade.response.OrderResponse;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsafran.vibetrader.exchange.ExchangeAccountType;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.ExchangeOrderSide;
import com.tsafran.vibetrader.exchange.InstrumentPrecision;
import com.tsafran.vibetrader.util.Util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class BybitUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private BybitUtil() {
    }

    public static List<MarketKlineEntry> getMarketKlineEntries(Object response) {
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

    public static OrderResponse getOrderResponse(Object response) {
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

    public static InstrumentPrecision extractInstrumentPrecision(Object response, String symbol) {
        GenericResponse<InstrumentInfoResult> genericResponse = MAPPER.convertValue(
                response,
                new TypeReference<>() {}
        );

        if (genericResponse.getRetCode() != 0) {
            throw new IllegalStateException(
                    "Bybit error: " + genericResponse.getRetCode() + " " + genericResponse.getRetMsg()
            );
        }

        InstrumentInfoResult result = genericResponse.getResult();
        if (result == null || result.getInstrumentEntries() == null) {
            return null;
        }

        for (InstrumentEntry entry : result.getInstrumentEntries()) {
            if (entry != null && symbol.equalsIgnoreCase(entry.getSymbol())
                    && entry.getLotSizeFilter() != null
                    && entry.getPriceFilter() != null) {
                BigDecimal basePrecision = Util.parseDecimal(entry.getLotSizeFilter().getQtyStep());
                if (basePrecision == null) {
                    basePrecision = Util.parseDecimal(entry.getLotSizeFilter().getBasePrecision());
                }
                BigDecimal tickSize = Util.parseDecimal(entry.getPriceFilter().getTickSize());
                if (basePrecision != null && tickSize != null) {
                    return new InstrumentPrecision(basePrecision, tickSize);
                }
            }
        }

        return null;
    }

    public static BigDecimal extractTotalAvailableBalance(Object response) {
        GenericResponse<Map<String, Object>> genericResponse = MAPPER.convertValue(
                response,
                new TypeReference<>() {}
        );

        if (genericResponse.getRetCode() != 0) {
            throw new IllegalStateException(
                    "Bybit error: " + genericResponse.getRetCode() + " " + genericResponse.getRetMsg()
            );
        }

        Map<String, Object> result = genericResponse.getResult();
        if (result == null) {
            return null;
        }

        Object listObj = result.get("list");
        if (!(listObj instanceof List<?> list) || list.isEmpty()) {
            return null;
        }

        Object firstEntry = list.getFirst();
        if (!(firstEntry instanceof Map<?, ?> firstMap)) {
            return null;
        }
        return Util.parseDecimal(firstMap.get("totalAvailableBalance"));
    }

    public static boolean hasOpenOrders(Object response) {
        GenericResponse<Map<String, Object>> genericResponse = MAPPER.convertValue(
                response,
                new TypeReference<>() {}
        );

        if (genericResponse.getRetCode() != 0) {
            throw new IllegalStateException(
                    "Bybit error: " + genericResponse.getRetCode() + " " + genericResponse.getRetMsg()
            );
        }

        Map<String, Object> result = genericResponse.getResult();
        if (result == null) {
            return false;
        }

        Object listObj = result.get("list");
        return (listObj instanceof List<?> list) && !list.isEmpty();
    }

    public static AccountType mapAccountType(ExchangeAccountType accountType) {
        return switch (accountType) {
            case UNIFIED -> AccountType.UNIFIED;
            case CONTRACT -> AccountType.CONTRACT;
            case SPOT -> AccountType.SPOT;
            case FUND -> AccountType.FUND;
            case OPTION -> AccountType.OPTION;
            case INVESTMENT -> AccountType.INVESTMENT;
        };
    }

    public static Side mapSide(ExchangeOrderSide side) {
        return switch (side) {
            case LONG -> Side.BUY;
            case SHORT -> Side.SELL;
        };
    }

    public static CategoryType mapCategory(ExchangeCategory category) {
        return switch (category) {
            case SPOT -> CategoryType.SPOT;
            case LINEAR -> CategoryType.LINEAR;
            case INVERSE -> CategoryType.INVERSE;
            case OPTION -> CategoryType.OPTION;
        };
    }

    public static MarketInterval mapInterval(ExchangeInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> MarketInterval.ONE_MINUTE;
            case THREE_MINUTES -> MarketInterval.THREE_MINUTES;
            case FIVE_MINUTES -> MarketInterval.FIVE_MINUTES;
            case FIFTEEN_MINUTES -> MarketInterval.FIFTEEN_MINUTES;
            case THIRTY_MINUTES -> MarketInterval.HALF_HOURLY;
            case ONE_HOUR -> MarketInterval.HOURLY;
            case TWO_HOURS -> MarketInterval.TWO_HOURLY;
            case FOUR_HOURS -> MarketInterval.FOUR_HOURLY;
            case SIX_HOURS -> MarketInterval.SIX_HOURLY;
            case TWELVE_HOURS -> MarketInterval.TWELVE_HOURLY;
            case ONE_DAY -> MarketInterval.DAILY;
            case ONE_WEEK -> MarketInterval.WEEKLY;
            case ONE_MONTH -> MarketInterval.MONTHLY;
        };
    }
}
