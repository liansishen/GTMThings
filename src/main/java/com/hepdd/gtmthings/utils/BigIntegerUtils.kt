package com.hepdd.gtmthings.utils

import java.math.BigInteger

class BigIntegerUtils {

    companion object {

        val BIG_INTEGER_MAX_LONG: BigInteger = BigInteger.valueOf(Long.Companion.MAX_VALUE)

        @JvmStatic
        fun getLongValue(bigInt: BigInteger): Long {
            if (bigInt > BIG_INTEGER_MAX_LONG) {
                return Long.Companion.MAX_VALUE
            }
            return bigInt.toLong()
        }
    }
}
