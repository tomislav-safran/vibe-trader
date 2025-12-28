package com.tsafran.vibetrader.indicators;

import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.Ohlcv;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNumFactory;
import org.ta4j.core.num.NumFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;

@Service
public class IndicatorService {
    private static final NumFactory NUM_FACTORY = DoubleNumFactory.getInstance();

    public List<IndicatorSeries> computeIndicatorSeries(
            List<Ohlcv> candles,
            ExchangeInterval interval,
            List<IndicatorConfig> indicators
    ) {
        if (indicators == null || indicators.isEmpty()) {
            return List.of();
        }
        if (candles == null || candles.isEmpty()) {
            throw new IllegalArgumentException("Candles are required for indicator calculation.");
        }

        BarSeries series = toSeries(candles, interval);
        int maxScale = resolveMaxCloseScale(candles);
        List<IndicatorSeries> results = new ArrayList<>();

        for (IndicatorConfig indicator : indicators) {
            if (indicator == null || indicator.type() == null) {
                throw new IllegalArgumentException("Indicator type is required.");
            }
            switch (indicator.type()) {
                case EMA -> results.add(computeEmaSeries(series, indicator, maxScale));
                default -> throw new IllegalArgumentException("Unsupported indicator: " + indicator.type());
            }
        }

        return results;
    }

    private IndicatorSeries computeEmaSeries(BarSeries series, IndicatorConfig indicator, int scale) {
        Integer period = indicator.period();
        if (period == null || period <= 0) {
            throw new IllegalArgumentException("EMA requires a positive period.");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator ema = new EMAIndicator(closePrice, period);
        List<String> values = new ArrayList<>(series.getBarCount());
        for (int i = 0; i < series.getBarCount(); i++) {
            if (i < period - 1) {
                values.add("NA");
                continue;
            }
            var num = ema.getValue(i);
            if (num.isNaN()) {
                values.add("NA");
                continue;
            }
            BigDecimal rounded = num.bigDecimalValue().setScale(scale, RoundingMode.HALF_UP);
            values.add(rounded.toPlainString());
        }
        return new IndicatorSeries("EMA(" + period + ")", values);
    }

    private BarSeries toSeries(List<Ohlcv> candles, ExchangeInterval interval) {
        Duration duration = toDuration(interval);
        List<Bar> bars = new ArrayList<>(candles.size());

        for (Ohlcv candle : candles) {
            Instant begin = Instant.ofEpochMilli(candle.startTime());
            Instant end = begin.plus(duration);
            BigDecimal amount = candle.turnover() != null ? candle.turnover() : candle.volume();
            bars.add(new BaseBar(
                    duration,
                    begin,
                    end,
                    NUM_FACTORY.numOf(candle.open()),
                    NUM_FACTORY.numOf(candle.high()),
                    NUM_FACTORY.numOf(candle.low()),
                    NUM_FACTORY.numOf(candle.close()),
                    NUM_FACTORY.numOf(candle.volume()),
                    NUM_FACTORY.numOf(amount),
                    0L
            ));
        }

        return new BaseBarSeriesBuilder()
                .withBars(bars)
                .withNumFactory(NUM_FACTORY)
                .build();
    }

    private Duration toDuration(ExchangeInterval interval) {
        return switch (interval) {
            case ONE_MINUTE -> Duration.ofMinutes(1);
            case THREE_MINUTES -> Duration.ofMinutes(3);
            case FIVE_MINUTES -> Duration.ofMinutes(5);
            case FIFTEEN_MINUTES -> Duration.ofMinutes(15);
            case THIRTY_MINUTES -> Duration.ofMinutes(30);
            case ONE_HOUR -> Duration.ofHours(1);
            case TWO_HOURS -> Duration.ofHours(2);
            case FOUR_HOURS -> Duration.ofHours(4);
            case SIX_HOURS -> Duration.ofHours(6);
            case TWELVE_HOURS -> Duration.ofHours(12);
            case ONE_DAY -> Duration.ofDays(1);
            case ONE_WEEK -> Duration.ofDays(7);
            case ONE_MONTH -> Duration.ofDays(30);
        };
    }

    private int resolveMaxCloseScale(List<Ohlcv> candles) {
        int maxScale = 0;
        for (Ohlcv candle : candles) {
            if (candle.close() != null) {
                maxScale = Math.max(maxScale, candle.close().scale());
            }
        }
        return maxScale;
    }
}
