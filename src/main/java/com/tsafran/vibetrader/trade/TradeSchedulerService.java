package com.tsafran.vibetrader.trade;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class TradeSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(TradeSchedulerService.class);

    private final TaskScheduler tradeTaskScheduler;
    private final TradeExecutionService tradeExecutionService;
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    public void scheduleTrade(String symbol, long intervalMinutes) {
        Objects.requireNonNull(symbol, "symbol");
        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException("intervalMinutes must be positive");
        }

        String normalizedSymbol = symbol.trim().toUpperCase();
        cancelExisting(normalizedSymbol);

        Duration interval = Duration.ofMinutes(intervalMinutes);
        ScheduledFuture<?> future = tradeTaskScheduler.scheduleAtFixedRate(
                () -> runTrade(normalizedSymbol),
                interval
        );

        scheduledJobs.put(normalizedSymbol, future);
        logger.info("Scheduled trade job for {} every {} minutes", normalizedSymbol, intervalMinutes);
    }

    public void cancelTrade(String symbol) {
        Objects.requireNonNull(symbol, "symbol");
        String normalizedSymbol = symbol.trim().toUpperCase();
        if (cancelExisting(normalizedSymbol)) {
            logger.info("Cancelled trade job for {}", normalizedSymbol);
        } else {
            logger.info("No trade job found for {}", normalizedSymbol);
        }
    }

    private void runTrade(String symbol) {
        try {
            String orderId = tradeExecutionService.craftAndPlaceTrade(symbol);
            if (orderId == null || orderId.isBlank()) {
                logger.info("No trade placed for {}", symbol);
            } else {
                logger.info("Trade placed for {} with order id {}", symbol, orderId);
            }
        } catch (Exception ex) {
            logger.error("Scheduled trade failed for {}", symbol, ex);
        }
    }

    private boolean cancelExisting(String symbol) {
        ScheduledFuture<?> existing = scheduledJobs.remove(symbol);
        if (existing == null) {
            return false;
        }
        return existing.cancel(false);
    }
}
