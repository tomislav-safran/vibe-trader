package com.tsafran.vibetrader.trade;

import com.tsafran.vibetrader.algo.AlgoStrategy;
import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.FuturesMarketOrderRequest;
import com.tsafran.vibetrader.position.PositionService;
import com.tsafran.vibetrader.position.ProposedPosition;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AlgoTradeExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(AlgoTradeExecutionService.class);

    private final Exchange exchange;
    private final PositionService positionService;
    private final Map<String, AlgoStrategy> algoStrategies;

    public String placeAlgoTrade(String symbol, String strategyBeanName) {
        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(strategyBeanName, "strategyBeanName");

        AlgoStrategy strategy = algoStrategies.get(strategyBeanName);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No strategy found with bean name: " + strategyBeanName +
                    ". Available strategies: " + algoStrategies.keySet()
            );
        }

        logger.info("Placing algo trade for symbol: {} using strategy: {}", symbol, strategyBeanName);
        if (exchange.hasOpenOrders(symbol)) {
            logger.info("Skipping trade: open order already exists for {}", symbol);
            return null;
        }

        ProposedPosition proposal = strategy.run(symbol);
        if (proposal == null) {
            logger.info("No trade opportunity returned by strategy.");
            return null;
        }

        FuturesMarketOrderRequest order = positionService.buildMarketOrder(proposal);
        logger.info("Final order request: {}", order);

        String orderId = exchange.placeFuturesMarketOrder(order);
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalStateException("Order placement returned no order id");
        }
        return orderId;
    }
}
