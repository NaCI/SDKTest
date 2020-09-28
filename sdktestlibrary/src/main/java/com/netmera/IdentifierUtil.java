package com.netmera;

import java.math.BigDecimal;
import java.util.Random;

class IdentifierUtil {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long BASE = (long)"abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ".length();
    private static final long REF_DATE_TIME = System.currentTimeMillis();
    private static final long REF_MACHINE_TIME = System.nanoTime();

    private IdentifierUtil() {
    }

    static String generateIdentifier() {
        return base62(generateDecimalValue());
    }

    private static String base62(BigDecimal valueToConvert) {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal base = new BigDecimal(BASE);

        StringBuilder sb;
        BigDecimal remainder;
        for(sb = new StringBuilder(""); valueToConvert.compareTo(zero) == 1; valueToConvert = valueToConvert.subtract(remainder).divide(base)) {
            remainder = valueToConvert.remainder(base);
            sb.append("abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(remainder.intValue()));
        }

        return sb.toString();
    }

    private static BigDecimal generateDecimalValue() {
        double machineTimeInSeconds = (double)(System.nanoTime() - REF_MACHINE_TIME) / Math.pow(10.0D, 9.0D);
        double realTimeInSeconds = (double)REF_DATE_TIME / Math.pow(10.0D, 3.0D) + machineTimeInSeconds;
        int random = (new Random()).nextInt(65535) + 1;
        String val = random + "" + realTimeInSeconds;
        BigDecimal value = new BigDecimal(val);
        value = value.multiply(new BigDecimal(Math.pow(10.0D, 9.0D)));
        return value;
    }
}