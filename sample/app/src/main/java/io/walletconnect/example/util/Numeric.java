package io.walletconnect.example.util;

import java.math.BigInteger;

public final class Numeric {
    public static String toHexStringNoPrefix(BigInteger value) {
        return value.toString(16);
    }
}
