package com.aurora.browser.logging;

import android.content.Context;
import android.util.Log;

import android.content.Context;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Centralized logging so the DevTools UI + a future on-disk ring buffer can
 * both consume the same stream. Wraps android.util.Log for now.
 */
public class LogManager {
    private static final String TAG = "LogManager";
    private static File logFile = null;
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    
    // Single-threaded executor ensures log writes don't block the UI thread or cause race conditions
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    /**
     * Call this once in your Application class (e.g., AuroraApplication) on launch.
     */
    public static void initializeFileLogging(Context context) {
        try {
            // Saves to: /Android/data/com.aurora.browser/files/logs/aurora.log
            File logDir = new File(context.getExternalFilesDir(null), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            logFile = new File(logDir, "aurora.log");
            
            // Optional: Clear previous logs on every fresh app launch to keep file sizes small
            if (logFile.exists()) {
                logFile.delete();
            }
            logFile.createNewFile();
            
            i(TAG, "File logging initialized at: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize log file", e);
        }
    }

    /**
     * Appends a line to the local log file in the background.
     */
    private static void writeToFile(String level, String tag, String message) {
        if (logFile == null) return;

        logExecutor.execute(() -> {
            String time = timestampFormat.format(new Date());
            String logLine = String.format("[%s] [%s/%s]: %s\n", time, level, tag, message);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logLine);
            } catch (IOException e) {
                // Fail silently to prevent recursive crash during logging
            }
        });
    }

    // --- Update your existing log wrappers to also write to the file ---

    public static void d(String tag, String message) {
        Log.d(tag, message);
        writeToFile("D", tag, message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
        writeToFile("I", tag, message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        writeToFile("W", tag, message);
    }

    public static void e(String tag, String message, Throwable tr) {
        String msg = message + (tr != null ? "\n" + Log.getStackTraceString(tr) : "");
        Log.e(tag, msg);
        writeToFile("E", tag, msg);
    }
}