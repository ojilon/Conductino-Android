package com.conductino.study.web;

import android.app.Activity;
import android.graphics.Bitmap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import com.conductino.study.api.BrowserBridge;
import com.conductino.study.logging.LogManager;

/**
 * Owns the single WebView instance and wires up:
 *   - the asset loader (serves assets/ui/** under https://appassets.androidplatform.net/)
 *   - the JS<->Java bridge (@JavascriptInterface)
 *   - native external loading to bypass Cloudflare/CORS
 */

public class WebViewHost {

    private final Activity activity;
    private final WebView webView;
    private BrowserBridge bridge;
    private BrowserUiCallback uiCallback; //New callback for ui updates

    //Interface to talk back to BrowserActivity's native UI
    public interface BrowserUiCallback {
        void onUrUpdated(String url);
        void onProgressUpdated(int progress);
    }

    public WebViewHost(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    public void setUiCallback(BrowserUiCallback callback) {
        this.uiCallback = callback;
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

        webView.setWebViewClient(new ConductinoWebViewClient(assetLoader));
        webView.setWebChromeClient(new ConductinoChromeClient());

        //Pass the asset loder AND the UI callback into custom clients
        webView.setWebViewClient(new ConductinoWebViewClient(assetLoader) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (uiCallback != null && !url.contains("appassets.androidplatform.net")) {
                    uiCallback.onUrUpdated(url);
                }
            }
        });

        webView.setWebChromeClient(new ConductinoChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (uiCallback != null) {
                    uiCallback.onProgressUpdated(newProgress);
                }
            }
        });

        // Expose the API surface to every UI page.
        bridge = new BrowserBridge(this);
        webView.addJavascriptInterface(bridge, "AuroraNative");

        LogManager.i("WebViewHost", "WebView attached with asset loader + bridge");
    }

    /** Load a specific UI state document. Called by StateManager (local HTML). */
    public void loadUi(String relativeIndexPath) {
        String url = "https://appassets.androidplatform.net/ui/" + relativeIndexPath;
        LogManager.d("WebViewHost", "loadUi -> " + url);
        webView.post(() -> webView.loadUrl(url));
    }

    //Load an external webpage natively (Bypasses CORS/Bot checks).
    public void loadExternalUrl(String url) {
        LogManager.d("WebViewHost", "loadExternalUrl -> " + url);
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
