package com.tsafran.vibetrader.trade;

import com.tsafran.vibetrader.ai.AiTradeProposal;
import com.tsafran.vibetrader.ai.AiTradeService;
import com.tsafran.vibetrader.ai.TradeAiConfigService;
import com.tsafran.vibetrader.ai.TradeAiSettings;
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
    private final TradeAiConfigService tradeAiConfigService;

    public String craftAndPlaceTrade(String symbol) {
        return craftAndPlaceTrade(symbol, null);
    }

    public String craftAndPlaceTrade(String symbol, String configName) {
        Objects.requireNonNull(symbol, "symbol");

        TradeAiSettings config = tradeAiConfigService.loadConfig(configName);
        logger.info("Placing trade for symbol: {}", symbol);
        if (exchange.hasOpenOrders(symbol)) {
            logger.info("Skipping trade: open order already exists for {}", symbol);
            return null;
        }
        String systemMessage = buildSystemMessage(config);
        String userMessage = buildUserMessage(symbol, config);

        logger.info("Prompting AI...");
        AiTradeProposal proposal = aiTradeService.proposeTrade(symbol, systemMessage, userMessage);
        logger.info("AI response: {}", proposal);
        if (proposal.certaintyPercent() < config.certaintyThreshold()) {
            logger.info(
                    "Trade skipped: certainty {} below threshold {}",
                    proposal.certaintyPercent(),
                    config.certaintyThreshold()
            );
            return null;
        }
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

    private String buildSystemMessage(TradeAiSettings config) {
        return "Strategy:\n" + config.strategy();
    }

    private String buildUserMessage(String symbol, TradeAiSettings config) {
        ExchangeInterval interval = config.candleLookbackInterval();
        List<Ohlcv> candles = exchange.getKlines(
                symbol,
                ExchangeCategory.LINEAR,
                interval,
                config.candleLookbackLimit()
        );

        if (candles == null || candles.isEmpty()) {
            throw new IllegalStateException("No candles returned for " + symbol);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Symbol: ").append(symbol).append('\n');
        builder.append("Interval: ").append(interval).append('\n');
        builder.append("Candles (startTime,open,high,low,close,volume):\n");
        for (Ohlcv candle : candles) {
            builder.append(candle.startTime()).append(',')
                    .append(candle.open()).append(',')
                    .append(candle.high()).append(',')
                    .append(candle.low()).append(',')
                    .append(candle.close()).append(',')
                    .append(candle.volume())
                    .append('\n');
        }
        return builder.toString();
    }
}
