package com.tsafran.vibetrader.shell;

import com.tsafran.vibetrader.trade.AiTradeExecutionService;
import com.tsafran.vibetrader.trade.AiTradeSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Command(command = "trade", description = "AI-driven trade commands")
public class TradeCommands {
    private final AiTradeExecutionService aiTradeExecutionService;
    private final AiTradeSchedulerService aiTradeSchedulerService;

    @Command(command = "place", description = "Craft and place a single AI trade for a symbol")
    public String place(
            @Option(longNames = "symbol") String symbol,
            @Option(longNames = "config", defaultValue = "default") String configName
    ) {
        String orderId = aiTradeExecutionService.craftAndPlaceTrade(symbol, configName);
        if (orderId == null || orderId.isBlank()) {
            return "No trade placed.";
        }
        return "Order placed: " + orderId;
    }

    @Command(command = "schedule", description = "Schedule recurring AI trades for a symbol")
    public String schedule(
            @Option(longNames = "symbol") String symbol,
            @Option(longNames = "minutes") long intervalMinutes,
            @Option(longNames = "config", defaultValue = "default") String configName
    ) {
        aiTradeSchedulerService.scheduleTrade(symbol, intervalMinutes, configName);
        return "Scheduled " + symbol + " every " + intervalMinutes + " minutes using " + configName + ".";
    }

    @Command(command = "cancel", description = "Cancel a scheduled AI trade for a symbol")
    public String cancel(@Option(longNames = "symbol") String symbol) {
        aiTradeSchedulerService.cancelTrade(symbol);
        return "Cancelled schedule for " + symbol + ".";
    }
}
