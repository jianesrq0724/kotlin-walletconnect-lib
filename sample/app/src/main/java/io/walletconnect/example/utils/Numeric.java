package io.walletconnect.example.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public final class Numeric {
    public static String toHexStringNoPrefix(BigInteger value) {
        return value.toString(16);
    }
}
