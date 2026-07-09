package com.aurora.browser.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small shared IO thread pool so navigation never blocks the UI thread. */
public final class NetExecutor {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);
    private NetExecutor() {}
    public static void io(Runnable r) { POOL.execute(r); }
}
