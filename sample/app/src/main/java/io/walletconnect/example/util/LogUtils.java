package io.walletconnect.example.util;

import android.util.Log;


public class LogUtils {
    public static final String TAG = "TAG";   //自定义TAG

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

}
