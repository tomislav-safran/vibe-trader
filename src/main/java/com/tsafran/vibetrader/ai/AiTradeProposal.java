package com.tsafran.vibetrader.ai;

import com.tsafran.vibetrader.position.ProposedPosition;

public record AiTradeProposal(
        String reasoning,
        int certaintyPercent,
        ProposedPosition proposedPosition
) {
    public AiTradeProposal {
        if (reasoning == null || reasoning.isBlank()) {
            throw new IllegalArgumentException("reasoning must be provided");
        }
        if (certaintyPercent < 0 || certaintyPercent > 100) {
            throw new IllegalArgumentException("certaintyPercent must be between 0 and 100");
        }
    }
}
