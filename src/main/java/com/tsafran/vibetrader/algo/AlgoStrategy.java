package com.tsafran.vibetrader.algo;

import com.tsafran.vibetrader.position.ProposedPosition;

public interface AlgoStrategy {
    String ENGULFING_CANDLE = "engulfingCandleStrategy";

    ProposedPosition run(String symbol);
}
