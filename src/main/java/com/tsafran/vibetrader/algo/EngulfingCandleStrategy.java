package com.tsafran.vibetrader.algo;

import com.tsafran.vibetrader.exchange.Exchange;
import com.tsafran.vibetrader.exchange.ExchangeCategory;
import com.tsafran.vibetrader.exchange.ExchangeInterval;
import com.tsafran.vibetrader.exchange.ExchangeOrderSide;
import com.tsafran.vibetrader.exchange.Ohlcv;
import com.tsafran.vibetrader.position.ProposedPosition;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component(AlgoStrategy.ENGULFING_CANDLE)
@RequiredArgsConstructor
public class EngulfingCandleStrategy implements AlgoStrategy {
    private final Exchange exchange;

    @Override
    public ProposedPosition run(String symbol) {
        final BigDecimal RR = new BigDecimal("1.1");

        List<Ohlcv> klines = exchange.getKlines(symbol, ExchangeCategory.LINEAR, ExchangeInterval.ONE_MINUTE, 3);
        if (klines.size() < 2) {
            return null;
        }

        klines = klines.stream()
                .sorted(Comparator.comparingLong(Ohlcv::startTime))
                .toList();

        Ohlcv prev = klines.get(klines.size() - 2);
        Ohlcv curr = klines.getLast();

        boolean prevBearish = prev.close().compareTo(prev.open()) < 0;
        boolean prevBullish = !prevBearish;
        boolean currBearish = curr.close().compareTo(curr.open()) < 0;
        boolean currBullish = !currBearish;

        BigDecimal prevBody = prev.close().subtract(prev.open()).abs();
        BigDecimal currBody = curr.close().subtract(curr.open()).abs();
        BigDecimal requiredBodySize = prevBody.multiply(new BigDecimal("2.0"));

        if (currBullish && prevBearish && currBody.compareTo(requiredBodySize) >= 0) {
            BigDecimal stopLoss = curr.low();
            BigDecimal risk = curr.close().subtract(stopLoss);
            BigDecimal takeProfit = curr.close().add(risk.multiply(RR));
            return new ProposedPosition(symbol, ExchangeOrderSide.LONG, curr.close(), takeProfit, stopLoss);
        }

        if (currBearish && prevBullish && currBody.compareTo(requiredBodySize) >= 0) {
            BigDecimal stopLoss = curr.high();
            BigDecimal risk = stopLoss.subtract(curr.close());
            BigDecimal takeProfit = curr.close().subtract(risk.multiply(RR));
            return new ProposedPosition(symbol, ExchangeOrderSide.SHORT, curr.close(), takeProfit, stopLoss);
        }

        return null;
    }
}
