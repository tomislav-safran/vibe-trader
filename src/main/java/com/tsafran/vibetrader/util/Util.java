package com.tsafran.vibetrader.util;

import java.math.BigDecimal;

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
}
