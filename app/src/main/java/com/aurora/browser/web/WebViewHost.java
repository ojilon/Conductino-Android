package com.aurora.browser.web;

import android.app.Activity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.webkit.WebViewAssetLoader;

import com.aurora.browser.api.BrowserBridge;
import com.aurora.browser.logging.LogManager;

/**
 * Owns the single WebView instance and wires up:
 *   - the asset loader (serves assets/ui/** under https://appassets.androidplatform.net/)
 *   - the JS<->Java bridge (@JavascriptInterface)
 *   - the custom WebViewClient that lets Java intercept/stream sub-resources.
 *
 * The frontend is NOT one iframe: StateManager swaps the top-level document
 * (welcome / search / results / document / devtools) by loading different
 * assets/ui/<state>/index.html into THIS webview.
 */
public class WebViewHost {

    private final Activity activity;
    private final WebView webView;
    private BrowserBridge bridge;

    public WebViewHost(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    @SuppressWarnings({"SetJavaScriptEnabled"})
    public void attach() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/", new WebViewAssetLoader.AssetsPathHandler(activity))
                .build();

        webView.setWebViewClient(new AuroraWebViewClient(assetLoader));
        webView.setWebChromeClient(new AuroraChromeClient());

        // Expose the API surface to every UI page.
        bridge = new BrowserBridge(this);
        webView.addJavascriptInterface(bridge, "AuroraNative");

        LogManager.i("WebViewHost", "WebView attached with asset loader + bridge");
    }

    /** Load a specific UI state document. Called by StateManager. */
    public void loadUi(String relativeIndexPath) {
        String url = "https://appassets.androidplatform.net/ui/" + relativeIndexPath;
        LogManager.d("WebViewHost", "loadUi -> " + url);
        webView.post(() -> webView.loadUrl(url));
    }

    /** Push a JSON payload down to the currently loaded UI. */
    public void emit(String eventName, String jsonPayload) {
        String js = "window.Aurora && window.Aurora.onEvent("
                + jsStr(eventName) + "," + jsonPayload + ");";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }

    public boolean goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    public void detach() {
        webView.removeJavascriptInterface("AuroraNative");
        webView.destroy();
    }

    private static String jsStr(String v) {
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
