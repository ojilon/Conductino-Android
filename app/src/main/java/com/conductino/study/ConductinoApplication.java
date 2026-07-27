package com.conductino.study;

import android.app.Application;

import com.conductino.study.core.NativeCore;
import com.conductino.study.logging.LogManager;
import com.conductino.study.settings.SettingsManager;

import android.util.Log;

/**
 * Process-wide singleton. Loads the C backend, boots settings + logging.
 */
public class ConductinoApplication extends Application {

    private static ConductinoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        LogManager.initializeFileLogging(this);
        LogManager.i("App", "Conductino Study starting up");

        // Load settings/*.json before anything else touches config.
        SettingsManager.get().load(this);

        // Bring the native (C) core online through JNI.
        NativeCore.get().boot(getFilesDir().getAbsolutePath());
    }

    public static ConductinoApplication get() {
        return instance;
    }
}
