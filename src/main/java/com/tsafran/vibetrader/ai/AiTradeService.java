package com.tsafran.vibetrader.ai;

public interface AiTradeService {
    AiTradeProposal proposeTrade(String symbol, String systemMessage, String userMessage);
}
