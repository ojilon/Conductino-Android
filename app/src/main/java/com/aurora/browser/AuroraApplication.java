package com.aurora.browser;

import android.app.Application;

import com.aurora.browser.core.NativeCore;
import com.aurora.browser.logging.LogManager;
import com.aurora.browser.settings.SettingsManager;

/**
 * Process-wide singleton. Loads the C backend, boots settings + logging.
 */
public class AuroraApplication extends Application {

    private static AuroraApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        LogManager.init(this);
        LogManager.i("App", "Aurora Browser starting up");

        // Load settings/*.json before anything else touches config.
        SettingsManager.get().load(this);

        // Bring the native (C) core online through JNI.
        NativeCore.get().boot(getFilesDir().getAbsolutePath());
    }

    public static AuroraApplication get() {
        return instance;
    }
}
