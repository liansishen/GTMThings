package com.hepdd.gtmthings.utils;

import java.math.BigInteger;

public class BigIntegerUtils {

    public static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    public static long getLongValue(BigInteger bigInt) {
        if (bigInt.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            return Long.MAX_VALUE;
        }
        return bigInt.longValue();
    }
}
