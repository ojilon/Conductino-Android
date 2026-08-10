package com.conductino.study;

import android.app.Application;

import com.conductino.study.core.NativeCore;
import com.conductino.study.downloads.DownloadStore;
import com.conductino.study.logging.LogManager;
import com.conductino.study.settings.SettingsManager;

/**
 * Process-wide singleton. Loads settings, downloads store, native core.
 */
public class ConductinoApplication extends Application {

    private static ConductinoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        LogManager.initializeFileLogging(this);
        LogManager.i("App", "Conductino Study starting up");

        SettingsManager.get().load(this);
        DownloadStore.get().init(this);

        NativeCore.get().boot(getFilesDir().getAbsolutePath());
    }

    public static ConductinoApplication get() {
        return instance;
    }
}
