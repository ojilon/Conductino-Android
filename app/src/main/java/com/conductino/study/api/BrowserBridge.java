package com.conductino.study.api;

import android.webkit.JavascriptInterface;

import com.conductino.study.logging.LogManager;
import com.conductino.study.net.NavigationController;
import com.conductino.study.settings.SettingsManager;
import com.conductino.study.state.BrowserState;
import com.conductino.study.state.StateManager;
import com.conductino.study.tabs.TabManager;
import com.conductino.study.tabs.TabSession;
import com.conductino.study.web.WebViewHost;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JS ↔ Java contract (window.AuroraNative).
 */
public class BrowserBridge {

    private final WebViewHost host;

    public BrowserBridge(WebViewHost host) {
        this.host = host;
        StateManager.get().bind(host);
    }

    @JavascriptInterface
    public void uiReady(String stateName) {
        LogManager.d("Bridge", "uiReady: " + stateName);
        StateManager.get().onUiReady();
    }

    @JavascriptInterface
    public void submitOmnibox(String text) {
        NavigationController.get().handleInput(text);
    }

    @JavascriptInterface
    public void selectSearchEngine(String engineId) {
        NavigationController.get().setEngine(engineId);
    }

    @JavascriptInterface
    public void setTheme(String themeId) {
        SettingsManager.get().setThemeId(themeId);
    }

    @JavascriptInterface
    public void openResult(String url) {
        NavigationController.get().handleInput(url);
    }

    @JavascriptInterface
    public String suggest(String partial) {
        return NavigationController.get().suggestions(partial);
    }

    @JavascriptInterface
    public void openDevTools() {
        NavigationController.get().openDevTools();
    }

    @JavascriptInterface
    public void switchTab(String idStr) {
        try {
            long id = Long.parseLong(idStr);
            if (!TabManager.get().switchTo(id)) return;
            TabSession s = TabManager.get().active();
            if (s.isEmpty()) {
                StateManager.get().transitionTo(BrowserState.WELCOME, null);
            } else {
                StateManager.get().transitionTo(BrowserState.EXTERNAL, s.currentUrl);
            }
        } catch (Exception e) {
            LogManager.e("Bridge", "switchTab failed", e);
        }
    }

    @JavascriptInterface
    public void closeTab(String idStr) {
        try {
            long id = Long.parseLong(idStr);
            TabManager.get().close(id);
            // Refresh tabs UI if still open
            if (StateManager.get().current() == BrowserState.TABS) {
                StateManager.get().transitionTo(BrowserState.TABS, tabsPayloadJson());
            }
        } catch (Exception e) {
            LogManager.e("Bridge", "closeTab failed", e);
        }
    }

    public static String tabsPayloadJson() {
        try {
            JSONObject root = new JSONObject();
            JSONArray arr = new JSONArray();
            for (TabSession t : TabManager.get().list()) {
                JSONObject o = new JSONObject();
                o.put("id", t.id);
                o.put("url", t.currentUrl != null ? t.currentUrl : "");
                o.put("title", t.title != null ? t.title : "New Tab");
                arr.put(o);
            }
            root.put("tabs", arr);
            root.put("activeId", TabManager.get().active().id);
            return root.toString();
        } catch (Exception e) {
            return "{\"tabs\":[],\"activeId\":0}";
        }
    }
}
