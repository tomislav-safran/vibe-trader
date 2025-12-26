package com.tsafran.vibetrader.trade;

import com.tsafran.vibetrader.ai.AiTradeProposal;
import com.tsafran.vibetrader.ai.AiTradeService;
import com.tsafran.vibetrader.ai.TradeAiConfig;
import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.exchange.Ohlcv;
import com.tsafran.vibetrader.position.PositionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TradeExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(TradeExecutionService.class);

    private final Exchange exchange;
    private final AiTradeService aiTradeService;
    private final PositionService positionService;

    public String craftAndPlaceTrade(String symbol) {
        Objects.requireNonNull(symbol, "symbol");

        logger.info("Placing trade for symbol: {}", symbol);
        String systemMessage = buildSystemMessage();
        String userMessage = buildUserMessage(symbol);

        logger.info("Prompting AI...");
        AiTradeProposal proposal = aiTradeService.proposeTrade(symbol, systemMessage, userMessage);
        logger.info("AI response: {}", proposal);
        if (proposal.proposedPosition() == null) {
            logger.info("No trade opportunity returned by AI.");
            return null;
        }

        FuturesMarketOrderRequest order = positionService.buildMarketOrder(proposal.proposedPosition());
        logger.info("Final order request: {}", order);

        String orderId = exchange.placeFuturesMarketOrder(order);
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalStateException("Order placement returned no order id");
        }
        return orderId;
    }

    private String buildSystemMessage() {
        return "Strategy:\n" + TradeAiConfig.STRATEGY;
    }

    private String buildUserMessage(String symbol) {
        ExchangeInterval interval = TradeAiConfig.CANDLE_LOOKBACK_INTERVAL;
        List<Ohlcv> candles = exchange.getKlines(
                symbol,
                ExchangeCategory.LINEAR,
                interval,
                TradeAiConfig.CANDLE_LOOKBACK_LIMIT
        );

        if (candles == null || candles.isEmpty()) {
            throw new IllegalStateException("No candles returned for " + symbol);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Symbol: ").append(symbol).append('\n');
        builder.append("Interval: ").append(interval).append('\n');
        builder.append("Candles (startTime,open,high,low,close,volume,turnover):\n");
        for (Ohlcv candle : candles) {
            builder.append(candle.startTime()).append(',')
                    .append(candle.open()).append(',')
                    .append(candle.high()).append(',')
                    .append(candle.low()).append(',')
                    .append(candle.close()).append(',')
                    .append(candle.volume()).append(',')
                    .append(candle.turnover())
                    .append('\n');
        }
        return builder.toString();
    }
}
