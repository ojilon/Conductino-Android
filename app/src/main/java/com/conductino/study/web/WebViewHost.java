package com.conductino.study.web;

import android.app.Activity;
import android.graphics.Bitmap;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.webkit.WebViewAssetLoader;

import com.conductino.study.api.BrowserBridge;
import com.conductino.study.downloads.DownloadStore;
import com.conductino.study.logging.LogManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns the single WebView instance and wires up:
 *   - asset loader (assets/ui/**)
 *   - JS bridge
 *   - external loads
 *   - download listener → internal DownloadStore
 */
public class WebViewHost {

    private final Activity activity;
    private final WebView webView;
    private BrowserBridge bridge;
    private BrowserUiCallback uiCallback;
    private final ExecutorService downloadExec = Executors.newSingleThreadExecutor();

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

        webView.setWebViewClient(new ConductinoWebViewClient(assetLoader) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (uiCallback != null && url != null
                        && !url.contains("appassets.androidplatform.net")) {
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

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                String name = URLUtil.guessFileName(url, contentDisposition, mimeType);
                LogManager.i("WebViewHost", "download start name=" + name + " url=" + url);
                downloadExec.execute(() -> fetchToStore(url, name, mimeType));
            }
        });

        bridge = new BrowserBridge(this);
        webView.addJavascriptInterface(bridge, "AuroraNative");

        LogManager.i("WebViewHost", "WebView attached");
    }

    /** Simple blocking fetch into the internal downloads store (v0). */
    private void fetchToStore(String urlStr, String fileName, String mimeType) {
        try {
            DownloadStore store = DownloadStore.get();
            File dest = store.allocateFile(fileName);
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) {
                    out.write(buf, 0, n);
                }
            }
            store.add(urlStr, dest.getName(), dest, mimeType);
            LogManager.i("WebViewHost", "download saved " + dest.getAbsolutePath());
        } catch (Exception e) {
            LogManager.e("WebViewHost", "download failed", e);
        }
    }

    public void loadUi(String relativeIndexPath) {
        String url = "https://appassets.androidplatform.net/ui/" + relativeIndexPath;
        LogManager.d("WebViewHost", "loadUi -> " + url);
        webView.post(() -> webView.loadUrl(url));
    }

    public void loadExternalUrl(String url) {
        LogManager.d("WebViewHost", "loadExternalUrl -> " + url);
        webView.post(() -> webView.loadUrl(url));
    }

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
        downloadExec.shutdownNow();
    }

    private static String jsStr(String v) {
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
