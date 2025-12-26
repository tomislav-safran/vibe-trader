package com.tsafran.vibetrader.shell;

import com.tsafran.vibetrader.trade.TradeExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Command(command = "trade", description = "AI-driven trade commands")
public class TradeCommands {
    private final TradeExecutionService tradeExecutionService;

    @Command(command = "place", description = "Craft and place a single AI trade for a symbol")
    public String place(@Option(longNames = "symbol") String symbol) {
        String orderId = tradeExecutionService.craftAndPlaceTrade(symbol);
        if (orderId == null || orderId.isBlank()) {
            return "No trade placed.";
        }
        return "Order placed: " + orderId;
    }
}
