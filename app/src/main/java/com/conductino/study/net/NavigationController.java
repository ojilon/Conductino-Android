package com.conductino.study.net;

import android.os.Handler;
import android.os.Looper;

import com.conductino.study.logging.LogManager;
import com.conductino.study.settings.SettingsManager;
import com.conductino.study.state.BrowserState;
import com.conductino.study.state.StateManager;

/**
 * Orchestrates a single navigation:
 *   plain text  -> URL or search query?
 *   URL         -> EXTERNAL state with that URL
 *   query       -> selected search engine queryUrl + EXTERNAL
 */
public class NavigationController {

    private static final NavigationController INSTANCE = new NavigationController();
    public static NavigationController get() { return INSTANCE; }
    private NavigationController() {}

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void setEngine(String id) {
        SettingsManager.get().setDefaultEngine(id);
        LogManager.i("Nav", "search engine -> " + SettingsManager.get().defaultEngine());
    }

    public String currentEngineId() {
        return SettingsManager.get().defaultEngine();
    }

    public void handleInput(String text) {
        if (text == null || text.trim().isEmpty()) return;

        String query = text.trim();
        String url;

        if (looksLikeUrl(query)) {
            url = (query.startsWith("http://") || query.startsWith("https://"))
                    ? query
                    : "https://" + query;
        } else {
            url = SettingsManager.get().buildSearchUrl(query);
        }

        LogManager.i("Nav", "navigate -> " + url);
        mainHandler.post(() -> StateManager.get().transitionTo(BrowserState.EXTERNAL, url));
    }

    private static boolean looksLikeUrl(String q) {
        if (q.contains(" ")) return false;
        if (q.startsWith("http://") || q.startsWith("https://")) return true;
        return q.contains(".") || q.startsWith("localhost");
    }

    public String suggestions(String partial) {
        // Typeahead later via engine suggestUrl
        return "[]";
    }

    public void openDevTools() {
        StateManager.get().transitionTo(BrowserState.DEVTOOLS, null);
    }
}
