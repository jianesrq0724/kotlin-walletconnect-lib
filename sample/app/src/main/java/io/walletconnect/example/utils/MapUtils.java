package io.walletconnect.example.utils;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class MapUtils {

    public static String getMapValueString(Map<String, String> map, @NonNull String name) {
        return getMapValueString(map, name, "");
    }

    public static String getMapValueString(Map<String, String> map, @NonNull String name, String defaultValue) {
        String value = defaultValue;
        if (map.containsKey(name)) {
            value = map.get(name);
        }
        return value;
    }

    public static long getMapValueLong(Map<String, Long> map, @NonNull String name) {
        long value = 0;
        if (map.containsKey(name)) {
            value = map.get(name);
        }
        return value;
    }


    public static double getMapValueDouble(Map<String, Double> map, @NonNull String name) {
        return getMapValueDouble(map, name, 0);
    }

    public static BigDecimal getMapValueBigDecimal(Map<String, BigDecimal> map, @NonNull String name) {
        return getMapValueBigDecimal(map, name, BigDecimal.ZERO);
    }

    public static double getMapValueDouble(Map<String, Double> map, @NonNull String name, double defaultValue) {
        double value = defaultValue;
        if (map.containsKey(name)) {
            value = map.get(name);
        }
        return value;
    }

    public static BigDecimal getMapValueBigDecimal(Map<String, BigDecimal> map, @NonNull String name, BigDecimal defaultValue) {
        BigDecimal value = defaultValue;
        if (map.containsKey(name)) {
            value = map.get(name);
        }
        return value;
    }

    public static BigInteger getMapValueBigInteger(Map<String, BigInteger> map, @NonNull String name) {
        BigInteger value = BigInteger.ZERO;
        if (map.containsKey(name)) {
            value = map.get(name);
        }
        return value;
    }


    public static int getMapValueInt(Map<String, Integer> map, String key) {
        return getMapValueInt(map, key, 0);
    }

    public static int getMapValueInt(Map<String, Integer> map, String key, int defaultValue) {
        int value = defaultValue;
        if (map.containsKey(key)) {
            value = map.get(key);
        }
        return value;
    }


    public static boolean getMapValueBoolean(Map<String, Boolean> map, String key) {
        return getMapValueBoolean(map, key, false);
    }

    private static boolean getMapValueBoolean(Map<String, Boolean> map, String key, boolean defaultValue) {
        boolean value = defaultValue;
        if (map.containsKey(key)) {
            value = map.get(key);
        }
        return value;
    }

    public static int addMapValueInt(Map<String, Integer> map, String key) {
        return addMapValueInt(map, key, 1);
    }

    public static long addMapValueLong(Map<String, Long> map, String key) {
        return addMapValueLong(map, key, 1);
    }

    public static int addMapValueInt(Map<String, Integer> map, String key, int defaultAddValue) {
        int value = getMapValueInt(map, key);
        value = value + defaultAddValue;
        map.put(key, value);
        return value;
    }

    public static double addMapValueDouble(Map<String, Double> map, String key, double defaultAddValue) {
        double value = getMapValueDouble(map, key);
        value = BigDecimal.valueOf(value).add(BigDecimal.valueOf(defaultAddValue)).doubleValue();
        map.put(key, value);
        return value;
    }

    public static double subtractMapValueDouble(Map<String, Double> map, String key, double defaultAddValue) {
        double value = getMapValueDouble(map, key);
        value = BigDecimal.valueOf(value).subtract(BigDecimal.valueOf(defaultAddValue)).doubleValue();
        map.put(key, value);
        return value;
    }

    public static double subtractMapValueDoubleZero(Map<String, Double> map, String key, double defaultAddValue) {
        double value = getMapValueDouble(map, key);
        value = BigDecimal.valueOf(value).subtract(BigDecimal.valueOf(defaultAddValue)).doubleValue();
        if (value < 0) {
            value = 0;
        }
        map.put(key, value);
        return value;
    }

    public static long addMapValueLong(Map<String, Long> map, String key, int defaultAddValue) {
        long value = getMapValueLong(map, key);
        value = value + defaultAddValue;
        map.put(key, value);
        return value;
    }

    public static int subtractMapValueInt(Map<String, Integer> map, String key) {
        int value = getMapValueInt(map, key);
        value--;
        if (value < 0) {
            map.put(key, 0);
        } else {
            map.put(key, value);
        }
        return value;
    }


    public static void removeMapValueInt(Map<String, Integer> map, String key) {
        map.remove(key);
    }

    public static void removeMapDouble(Map<String, Double> map, String key) {
        map.remove(key);
    }


}
