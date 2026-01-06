package com.tsafran.vibetrader.shell;

import com.tsafran.vibetrader.trade.AlgoTradeExecutionService;
import com.tsafran.vibetrader.trade.AlgoTradeSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Command(command = "algo", description = "Algorithm-driven trade commands")
public class AlgoTradeCommands {
    private final AlgoTradeExecutionService algoTradeExecutionService;
    private final AlgoTradeSchedulerService algoTradeSchedulerService;

    @Command(command = "place", description = "Place a single algo trade for a symbol using a strategy")
    public String place(
            @Option(longNames = "symbol") String symbol,
            @Option(longNames = "strategy") String strategyBeanName
    ) {
        String orderId = algoTradeExecutionService.placeAlgoTrade(symbol, strategyBeanName);
        if (orderId == null || orderId.isBlank()) {
            return "No algo trade placed.";
        }
        return "Algo order placed: " + orderId;
    }

    @Command(command = "schedule", description = "Schedule recurring algo trades for one or more symbols")
    public String schedule(
            @Option(longNames = "symbol") String symbols,
            @Option(longNames = "minutes") long intervalMinutes,
            @Option(longNames = "strategy") String strategyBeanName
    ) {
        String[] symbolList = symbols.split("[\\s,]+", -1);
        int scheduledCount = 0;
        for (String symbol : symbolList) {
            if (symbol == null || symbol.isBlank()) {
                continue;
            }
            algoTradeSchedulerService.scheduleTrade(symbol, intervalMinutes, strategyBeanName);
            scheduledCount++;
        }
        if (scheduledCount == 0) {
            return "No symbols provided to schedule.";
        }
        return "Scheduled algo trade for " + scheduledCount + " symbol(s) every " + intervalMinutes
                + " minutes using strategy " + strategyBeanName + ".";
    }

    @Command(command = "cancel", description = "Cancel a scheduled algo trade for a symbol")
    public String cancel(@Option(longNames = "symbol") String symbol) {
        algoTradeSchedulerService.cancelTrade(symbol);
        return "Cancelled algo schedule for " + symbol + ".";
    }
}
