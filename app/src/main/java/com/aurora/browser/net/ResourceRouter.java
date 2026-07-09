package com.aurora.browser.net;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.aurora.browser.logging.LogManager;
import com.aurora.browser.settings.SettingsManager;

import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Decides, per sub-resource, whether to STREAM bytes straight to the WebView
 * (css/js/img/font — most things) or DOWNLOAD-then-hand-off. Streaming keeps
 * memory low and lets the page paint progressively.
 */
public class ResourceRouter {

    private static final ResourceRouter INSTANCE = new ResourceRouter();
    public static ResourceRouter get() { return INSTANCE; }
    private ResourceRouter() {}

    public WebResourceResponse route(WebResourceRequest request) {
        String url = request.getUrl().toString();
        try {
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", SettingsManager.get().userAgent())
                    .build();
            Response resp = Fetcher.get().client().newCall(req).execute();

            String contentType = resp.header("Content-Type", "application/octet-stream");
            String mime = contentType.split(";")[0].trim();
            String enc = "utf-8";

            InputStream stream = resp.body() != null ? resp.body().byteStream() : null;
            LogManager.d("ResourceRouter", "stream " + mime + " <- " + url);
            // Returning the live stream = progressive streaming into the renderer.
            return new WebResourceResponse(mime, enc, stream);
        } catch (Exception e) {
            LogManager.e("ResourceRouter", "route failed " + url, e);
            return null; // let WebView handle/fail normally
        }
    }
}
