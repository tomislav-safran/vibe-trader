package com.tsafran.vibetrader.shell;

import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.ExchangeOrderSide;
import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.exchange.Ohlcv;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
@Command(command = "bybit", description = "Bybit exchange commands")
public class BybitCommands {
    private final Exchange bybitExchange;

    public BybitCommands(Exchange bybitExchange) {
        this.bybitExchange = bybitExchange;
    }

    @Command(command = "klines", description = "Fetch OHLCV klines from Bybit")
    public String klines(
            @Option(longNames = "symbol") String symbol,
            @Option(longNames = "category") String category,
            @Option(longNames = "interval") String interval,
            @Option(longNames = "limit", defaultValue = "200") int limit
    ) {
        ExchangeCategory exchangeCategory = parseCategory(category);
        ExchangeInterval exchangeInterval = parseInterval(interval);

        List<Ohlcv> klines = bybitExchange.getKlines(symbol, exchangeCategory, exchangeInterval, limit);
        if (klines.isEmpty()) {
            return "No klines returned.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("startTime,open,high,low,close,volume,turnover");
        for (Ohlcv kline : klines) {
            builder.append(System.lineSeparator())
                    .append(kline.startTime()).append(',')
                    .append(kline.open()).append(',')
                    .append(kline.high()).append(',')
                    .append(kline.low()).append(',')
                    .append(kline.close()).append(',')
                    .append(kline.volume()).append(',')
                    .append(kline.turnover());
        }
        return builder.toString();
    }

    @Command(command = "order", description = "Place a market futures order with TP/SL on Bybit")
    public String order(
            @Option(longNames = "symbol") String symbol,
            @Option(longNames = "side") String side,
            @Option(longNames = "qty") BigDecimal quantity,
            @Option(longNames = "tp") BigDecimal takeProfit,
            @Option(longNames = "sl") BigDecimal stopLoss,
            @Option(longNames = "category", defaultValue = "linear") String category
    ) {
        ExchangeCategory exchangeCategory = parseCategory(category);
        ExchangeOrderSide orderSide = parseOrderSide(side);

        FuturesMarketOrderRequest request = new FuturesMarketOrderRequest(
                symbol,
                exchangeCategory,
                orderSide,
                quantity,
                takeProfit,
                stopLoss
        );

        String orderId = bybitExchange.placeFuturesMarketOrder(request);
        return orderId == null ? "Order placed, no order id returned." : "Order placed: " + orderId;
    }

    private static ExchangeCategory parseCategory(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return ExchangeCategory.valueOf(normalized);
    }

    private static ExchangeInterval parseInterval(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "1m", "1min", "1minute" -> ExchangeInterval.ONE_MINUTE;
            case "3", "3m", "3min", "3minute" -> ExchangeInterval.THREE_MINUTES;
            case "5", "5m", "5min", "5minute" -> ExchangeInterval.FIVE_MINUTES;
            case "15", "15m", "15min", "15minute" -> ExchangeInterval.FIFTEEN_MINUTES;
            case "30", "30m", "30min", "30minute" -> ExchangeInterval.THIRTY_MINUTES;
            case "60", "60m", "1h", "1hour" -> ExchangeInterval.ONE_HOUR;
            case "120", "120m", "2h", "2hour" -> ExchangeInterval.TWO_HOURS;
            case "240", "240m", "4h", "4hour" -> ExchangeInterval.FOUR_HOURS;
            case "360", "360m", "6h", "6hour" -> ExchangeInterval.SIX_HOURS;
            case "720", "720m", "12h", "12hour" -> ExchangeInterval.TWELVE_HOURS;
            case "d", "1d", "day", "1day" -> ExchangeInterval.ONE_DAY;
            case "w", "1w", "week", "1week" -> ExchangeInterval.ONE_WEEK;
            case "mo", "1mo", "month", "1month" -> ExchangeInterval.ONE_MONTH;
            default -> throw new IllegalArgumentException("Unsupported interval: " + value);
        };
    }

    private static ExchangeOrderSide parseOrderSide(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "long", "buy" -> ExchangeOrderSide.LONG;
            case "short", "sell" -> ExchangeOrderSide.SHORT;
            default -> throw new IllegalArgumentException("Unsupported side: " + value);
        };
    }
}
