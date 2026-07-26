package com.aurora.browser.net;

import android.os.Handler;
import android.os.Looper;

import com.aurora.browser.core.NativeCore;
import com.aurora.browser.logging.LogManager;
import com.aurora.browser.settings.SettingsManager;
import com.aurora.browser.state.BrowserState;
import com.aurora.browser.state.StateManager;

/**
 * Orchestrates a single navigation:
 *   plain text  -> is it a URL or a query?
 *   URL         -> trigger native WebView load
 *   query       -> hit search engine -> trigger native WebView load
 */

public class NavigationController {

    private static final NavigationController INSTANCE = new NavigationController();
    public static NavigationController get() { return INSTANCE; }
    private NavigationController() {}

    private String engineId = SettingsManager.get().defaultEngine();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void setEngine(String id) {
        this.engineId = id;
        LogManager.i("Nav", "search engine -> " + id);
    }

    /** 
     * Parses input and routes it natively to bypass bot-protection.
     * */
    public void handleInput(String text) {
        if (text == null || text.trim().isEmpty()) return;

        String query = text.trim();
        String url;

        //Basic Heuristic: URL vs Search
        if (!query.contains(" ") && query.contains(".")) {
            url = (!query.startsWith("http://") && !query.startsWith("https://")) ? "https://" + query : query;
        }else {
            //Route through selected search engine (for noe google as defualt)
            url = "https://www.google.com/search?q=" + query;
        }

        LogManager.i("Nav", "Routing native navigation to: " + url);

        /**
         * Inform the stateManager to handle an EXTERNAM state, passing the URL as the payload.
         * (add external to BrowserState enum
         * */
         mainHandler.post(() -> StateManager.get().transitionTo(BrowserState.EXTERNAL, url));
    }

    public String suggestions(String partial) {
        // We will move this to C later. Leave as is for now, or return "[]" to disable temporarily.
        return "[]";
    }

    public void openDevTools() {
        StateManager.get().transitionTo(BrowserState.DEVTOOLS, null);
    }
}
