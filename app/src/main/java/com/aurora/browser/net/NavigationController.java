package com.aurora.browser.net;

import com.aurora.browser.core.NativeCore;
import com.aurora.browser.logging.LogManager;
import com.aurora.browser.settings.SettingsManager;
import com.aurora.browser.state.BrowserState;
import com.aurora.browser.state.StateManager;

/**
 * Orchestrates a single navigation:
 *   plain text  -> is it a URL or a query?
 *   URL         -> fetch document, classify, render DOCUMENT state
 *   query       -> hit search engine, parse results, render RESULTS state
 *
 * Runs off the main thread; posts state transitions back.
 */
public class NavigationController {

    private static final NavigationController INSTANCE = new NavigationController();
    public static NavigationController get() { return INSTANCE; }
    private NavigationController() {}

    private String engineId = SettingsManager.get().defaultEngine();

    public void setEngine(String id) {
        this.engineId = id;
        LogManager.i("Nav", "search engine -> " + id);
    }

    public void handleInput(String text) {
        NetExecutor.io(() -> {
            //send raw text directly to the C backend
            String jsonPayload = NativeCore.get().processRequest(text);

            // For now, assume C returns a DOCUMENT payload. 
            // Later, C will return specific JSON indicating if it's an ERROR, RESULTS, or DOCUMENT.
            StateManager.get().transitionTo(BrowserState.DOCUMENT, jsonPayload);
        });
    }

    public String suggestions(String partial) {
        // We will move this to C later. Leave as is for now, or return "[]" to disable temporarily.
        return "[]";
    }

    public void openDevTools() {
        StateManager.get().transitionTo(BrowserState.DEVTOOLS, null);
    }
}
