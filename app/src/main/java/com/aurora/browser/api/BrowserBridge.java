package com.aurora.browser.api;

import android.webkit.JavascriptInterface;

import com.aurora.browser.logging.LogManager;
import com.aurora.browser.net.NavigationController;
import com.aurora.browser.state.StateManager;
import com.aurora.browser.web.WebViewHost;

/**
 * THE contract between the frontend JS and Java. Injected as window.AuroraNative.
 *
 * The JS side keeps almost no logic: it collects input and calls these methods.
 * Everything heavy (search, classify, fetch, stream/download, state routing)
 * happens on the Java side, which in turn offloads CPU-bound work to the C core.
 *
 * Mirror JS helper lives in assets/ui/shared/aurora-api.js (window.Aurora).
 */
public class BrowserBridge {

    private final WebViewHost host;

    public BrowserBridge(WebViewHost host) {
        this.host = host;
        StateManager.get().bind(host);
    }

    /** JS calls this on DOMContentLoaded of each UI page. */
    @JavascriptInterface
    public void uiReady(String stateName) {
        LogManager.d("Bridge", "uiReady: " + stateName);
        StateManager.get().onUiReady();
    }

    /** The URL bar submitted plain text or a URL. */
    @JavascriptInterface
    public void submitOmnibox(String text) {
        LogManager.d("Bridge", "submitOmnibox: " + text);
        NavigationController.get().handleInput(text);
    }

    /** User picked a different search engine in the search UI. */
    @JavascriptInterface
    public void selectSearchEngine(String engineId) {
        NavigationController.get().setEngine(engineId);
    }

    /** Open a specific result URL from the results list. */
    @JavascriptInterface
    public void openResult(String url) {
        NavigationController.get().navigate(url);
    }

    /** Return typeahead suggestions as JSON (frontend renders them). */
    @JavascriptInterface
    public String suggest(String partial) {
        return NavigationController.get().suggestions(partial);
    }

    /** Toggle DevTools UI state. */
    @JavascriptInterface
    public void openDevTools() {
        NavigationController.get().openDevTools();
    }
}
