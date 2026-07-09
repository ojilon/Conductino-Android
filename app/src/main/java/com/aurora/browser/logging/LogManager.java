package com.aurora.browser.logging;

import android.content.Context;
import android.util.Log;

/**
 * Centralized logging so the DevTools UI + a future on-disk ring buffer can
 * both consume the same stream. Wraps android.util.Log for now.
 */
public final class LogManager {

    private static final String PREFIX = "Aurora/";
    private LogManager() {}

    public static void init(Context ctx) {
        i("Log", "LogManager initialized");
        // Future: attach a file appender in ctx.getFilesDir()/logs/.
    }

    public static void d(String tag, String msg) { Log.d(PREFIX + tag, msg); }
    public static void i(String tag, String msg) { Log.i(PREFIX + tag, msg); }
    public static void w(String tag, String msg) { Log.w(PREFIX + tag, msg); }

    public static void e(String tag, String msg) { Log.e(PREFIX + tag, msg); }
    public static void e(String tag, String msg, Throwable t) { Log.e(PREFIX + tag, msg, t); }
}
