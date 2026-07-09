package com.aurora.browser.net;

import com.aurora.browser.logging.LogManager;
import com.aurora.browser.settings.SettingsManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Network fetch layer. For top-level documents it DOWNLOADS the body so Java
 * can classify + optionally hand HTML to the C parser. Sub-resources are NOT
 * fetched here — they are streamed on demand by ResourceRouter as the page
 * paints (see AuroraWebViewClient.shouldInterceptRequest).
 */
public class Fetcher {

    private static final Fetcher INSTANCE = new Fetcher();
    public static Fetcher get() { return INSTANCE; }

    private final OkHttpClient client;

    private Fetcher() {
        this.client = new OkHttpClient.Builder().build();
    }

    public FetchResult fetchDocument(String url) {
        String ua = SettingsManager.get().userAgent();
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", ua)
                .build();
        try (Response resp = client.newCall(req).execute()) {
            FetchResult r = new FetchResult();
            r.url = url;
            r.ok = resp.isSuccessful();
            r.status = resp.code();
            r.mimeType = resp.header("Content-Type", "text/html");
            r.body = resp.body() != null ? resp.body().string() : "";
            return r;
        } catch (Exception e) {
            LogManager.e("Fetcher", "fetch failed " + url, e);
            return FetchResult.error(url, e.getMessage());
        }
    }

    public OkHttpClient client() {
        return client;
    }
}
