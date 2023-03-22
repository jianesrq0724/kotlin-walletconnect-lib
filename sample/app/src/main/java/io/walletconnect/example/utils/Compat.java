package io.walletconnect.example.utils;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.Charset;

public final class Compat {

    /** Ports {@link java.nio.charset.StandardCharsets#UTF_8}. */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

    /**
     * The number of bytes used to represent a {@code long} value in two's complement binary form.
     *
     * @since 1.8
     */
    public static final int BYTES = Long.SIZE / Byte.SIZE;

    private Compat() {}

    /** Ports {@link String#join(CharSequence, CharSequence...)}. */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        if (elements.length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(7 * elements.length);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter);
            sb.append(elements[i]);
        }
        return sb.toString();
    }

    /** Ports {@link BigInteger#longValueExact()}. */
    public static long longValueExact(BigInteger value) {
        if (value.compareTo(LONG_MIN) >= 0 && value.compareTo(LONG_MAX) <= 0) {
            return value.longValue();
        }
        throw new ArithmeticException("BigInteger out of long range");
    }

    /** Ports {@link java.lang.reflect.Type#getTypeName()}. */
    public static String getTypeName(Type type) {
        if (type instanceof Class) {
            return getClassName((Class) type);
        }
        return type.toString();
    }

    /** Copied from {@link Class#getTypeName()}. */
    private static String getClassName(Class type) {
        if (type.isArray()) {
            try {
                Class<?> cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
                /*FALLTHRU*/
            }
        }
        return type.getName();
    }

    /** Ports {@link Boolean#hashCode(boolean)}. */
    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    /** Ports Objects.requireNonNull(T obj) */
    public static <T> T requireNonNull(T obj) {
        if (obj == null) throw new NullPointerException();
        return obj;
    }

    /** Ports Objects.equals(Object a, Object b) */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
