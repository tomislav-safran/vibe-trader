package com.tsafran.vibetrader.exchange.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.MarketInterval;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.ExchangeMarketMapper;
import org.springframework.stereotype.Component;

@Component
public class BybitMarketMapper implements ExchangeMarketMapper<CategoryType, MarketInterval> {
    @Override
    public CategoryType mapCategory(ExchangeCategory category) {
        return switch (category) {
            case SPOT -> CategoryType.SPOT;
            case LINEAR -> CategoryType.LINEAR;
            case INVERSE -> CategoryType.INVERSE;
            case OPTION -> CategoryType.OPTION;
        };
    }

    @Override
    public MarketInterval mapInterval(ExchangeInterval interval) {
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
