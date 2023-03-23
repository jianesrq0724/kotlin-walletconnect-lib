package io.walletconnect.example.utils;


import java.math.BigInteger;

public class EthUtils {

    public static String getHexStr(String value) {
        BigInteger valueBigInteger = new BigInteger(value);
        String hexValue = Numeric.toHexStringNoPrefix(valueBigInteger);
        return "0x" + hexValue;
    }


}
