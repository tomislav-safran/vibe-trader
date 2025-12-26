package com.tsafran.vibetrader.ai;

import com.tsafran.vibetrader.position.ProposedPosition;

public record AiTradeProposal(
        String reasoning,
        ProposedPosition proposedPosition
) {
    public AiTradeProposal {
        if (reasoning == null || reasoning.isBlank()) {
            throw new IllegalArgumentException("reasoning must be provided");
        }
    }
}
