package com.tsafran.vibetrader.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import com.tsafran.vibetrader.exchange.ExchangeOrderSide;

public final class Util {
    private Util() {
    }

    public static BigDecimal parseDecimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    public static BigDecimal parseDecimal(Object value) {
        return switch (value) {
            case null -> null;
            case BigDecimal decimal -> decimal;
            case Number number -> BigDecimal.valueOf(number.doubleValue());
            default -> new BigDecimal(value.toString());
        };
    }

    public static ExchangeOrderSide parseOrderSide(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("side must be provided");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "long", "buy" -> ExchangeOrderSide.LONG;
            case "short", "sell" -> ExchangeOrderSide.SHORT;
            default -> throw new IllegalArgumentException("Unsupported side: " + value);
        };
    }

    public static BigDecimal roundToStep(BigDecimal value, BigDecimal step) {
        return roundToStep(value, step, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundDownToStep(BigDecimal value, BigDecimal step) {
        return roundToStep(value, step, RoundingMode.DOWN);
    }

    private static BigDecimal roundToStep(BigDecimal value, BigDecimal step, RoundingMode roundingMode) {
        if (value == null) {
            return null;
        }
        if (step == null || step.signum() <= 0) {
            throw new IllegalArgumentException("step must be positive");
        }
        BigDecimal multiplier = value.divide(step, 0, roundingMode);
        return multiplier.multiply(step);
    }
}
