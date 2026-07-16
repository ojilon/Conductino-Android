package com.aurora.browser.web;

import android.util.Log; //Added for debugging
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
//import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat; // instead of android.webkit.WebViewClient;

import com.aurora.browser.net.ResourceRouter;

/**
 * Intercepts every resource the WebView requests.
 *   1. If it targets our virtual asset origin -> served from assets/ui/**.
 *   2. Otherwise -> handed to ResourceRouter, which decides whether Java
 *      DOWNLOADS the bytes (e.g. the top-level HTML document) or STREAMS
 *      them straight through (css/js/img/font) while the page paints.
 */
public class AuroraWebViewClient extends WebViewClientCompat {

    private final WebViewAssetLoader assetLoader;

    public AuroraWebViewClient(WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();


        //Try Local UI assets first.
        WebResourceResponse local = assetLoader.shouldInterceptRequest(request.getUrl());
        if (local != null) {
            Log.d("AuroraWebViewClient", "Served locally: " + url);
            return local;
        }

        // 2. Safety catch: If it targets our virtual domain but failed to find the asset, 
        // DO NOT send it to ResourceRouter. Prevent the ERR_INVALID_RESPONSE crash.
        if (url.contains("appassets.androidplatform.net")) {
            Log.e("AuroraWebViewClient", "FAILED to find local asset: " + url);
            // Returning an empty response so it gracefully loads a blank page or 404
            // instead of breaking WebView with an invalid socket response
            return new WebResourceResponse("text/html", "UTF-8", null);
        }

        // Remote resources: Java decides stream vs. download.
        Log.d("AuroraWebViewClient", "Routing remotely:" + url);
        return ResourceRouter.get().route(request);
    }
}
