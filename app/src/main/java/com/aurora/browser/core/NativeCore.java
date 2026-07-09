package com.aurora.browser.core;

import com.aurora.browser.logging.LogManager;

/**
 * The single JNI gateway into the C backend (libaurora_core.so).
 *
 * The C side handles CPU/IO-heavy work the JVM shouldn't:
 *   - fast HTML tokenizing / search-result extraction
 *   - SQLite history/bookmarks/cache (via the sqlite3 C lib)
 *   - future: text search indexing, media demux, pdf parsing (see cpp/features)
 *
 * Native methods below are implemented in app/src/main/cpp/*.c.
 */
public final class NativeCore {

    private static final NativeCore INSTANCE = new NativeCore();
    public static NativeCore get() { return INSTANCE; }

    static {
        try {
            System.loadLibrary("aurora_core");
        } catch (Throwable t) {
            LogManager.e("NativeCore", "native lib load failed", t);
        }
    }

    private NativeCore() {}

    /** Boot the C runtime, open the SQLite DB in appDir. */
    public void boot(String appDir) {
        int rc = nativeBoot(appDir);
        LogManager.i("NativeCore", "nativeBoot rc=" + rc);
    }

    // ── native declarations ────────────────────────────────
    private native int nativeBoot(String appDir);

    /** Parse engine HTML into a JSON result array, capped at 'limit'. */
    public native String parseSearchResults(String html, String selector, int limit);

    /** Persist a visited page into history (SQLite). */
    public native void addHistory(String url, String title, long timestamp);

    /** Return recent history as JSON. */
    public native String recentHistory(int limit);

    /** libc/library version banner for the About screen. */
    public native String versionInfo();
}
