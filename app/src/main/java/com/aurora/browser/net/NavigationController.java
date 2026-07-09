package com.aurora.browser.net;

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
            if (UrlClassifier.looksLikeUrl(text)) {
                navigate(UrlClassifier.normalize(text));
            } else {
                runSearch(text);
            }
        });
    }

    public void navigate(String url) {
        NetExecutor.io(() -> {
            FetchResult res = Fetcher.get().fetchDocument(url);
            ResponseType type = ResponseClassifier.classify(res);
            switch (type) {
                case HTML_DOCUMENT:
                    StateManager.get().transitionTo(BrowserState.DOCUMENT, res.toJson());
                    break;
                case MEDIA:
                    StateManager.get().transitionTo(BrowserState.DOCUMENT, res.toMediaJson());
                    break;
                case ERROR:
                default:
                    StateManager.get().transitionTo(BrowserState.ERROR, res.errorJson());
            }
        });
    }

    private void runSearch(String query) {
        int limit = SettingsManager.get().maxSearchResults();
        SearchResultSet set = SearchEngineClient.get().search(engineId, query, limit);
        if (set.size() == 1 && set.isDirectHit()) {
            navigate(set.first().url);            // "gmail" -> straight to site
        } else {
            StateManager.get().transitionTo(BrowserState.RESULTS, set.toJson());
        }
    }

    public String suggestions(String partial) {
        return SearchEngineClient.get().suggest(engineId, partial);
    }

    public void openDevTools() {
        StateManager.get().transitionTo(BrowserState.DEVTOOLS, null);
    }
}
