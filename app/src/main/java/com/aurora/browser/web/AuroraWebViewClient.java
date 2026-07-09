package com.aurora.browser.web;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import com.aurora.browser.net.ResourceRouter;

/**
 * Intercepts every resource the WebView requests.
 *   1. If it targets our virtual asset origin -> served from assets/ui/**.
 *   2. Otherwise -> handed to ResourceRouter, which decides whether Java
 *      DOWNLOADS the bytes (e.g. the top-level HTML document) or STREAMS
 *      them straight through (css/js/img/font) while the page paints.
 */
public class AuroraWebViewClient extends WebViewClient {

    private final WebViewAssetLoader assetLoader;

    public AuroraWebViewClient(WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        // Local UI assets first.
        WebResourceResponse local = assetLoader.shouldInterceptRequest(request.getUrl());
        if (local != null) {
            return local;
        }
        // Remote resources: Java decides stream vs. download.
        return ResourceRouter.get().route(request);
    }
}
