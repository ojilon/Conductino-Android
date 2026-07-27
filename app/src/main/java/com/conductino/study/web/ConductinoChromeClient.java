package com.conductino.study.web;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

import com.conductino.study.logging.LogManager;

/**
 * Surfaces JS console output into LogManager so the DevTools UI state can
 * mirror it, and reports load progress back to whatever UI is active.
 */
public class ConductinoChromeClient extends WebChromeClient {

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        LogManager.d("JSConsole",
                cm.message() + " @ " + cm.sourceId() + ":" + cm.lineNumber());
        return true;
    }
}
