package com.aurora.browser.net;

import com.aurora.browser.core.NativeCore;
import com.aurora.browser.logging.LogManager;
import com.aurora.browser.settings.SettingsManager;

/**
 * Runs a query against the chosen engine, then hands the raw HTML to the C
 * core's HTML tokenizer to extract results quickly (heavy string work in C).
 */
public class SearchEngineClient {

    private static final SearchEngineClient INSTANCE = new SearchEngineClient();
    public static SearchEngineClient get() { return INSTANCE; }
    private SearchEngineClient() {}

    public SearchResultSet search(String engineId, String query, int limit) {
        String queryUrl = SettingsManager.get().engineQueryUrl(engineId)
                .replace("{query}", urlEncode(query));
        LogManager.i("Search", "engine=" + engineId + " q=" + query);

        FetchResult res = Fetcher.get().fetchDocument(queryUrl);
        // Offload result parsing to C (fast HTML scanning). Returns JSON.
        String selector = SettingsManager.get().engineResultSelector(engineId);
        String json = NativeCore.get().parseSearchResults(res.body, selector, limit);
        return SearchResultSet.fromJson(json, query);
    }

    public String suggest(String engineId, String partial) {
        String url = SettingsManager.get().engineSuggestUrl(engineId)
                .replace("{query}", urlEncode(partial));
        return Fetcher.get().fetchDocument(url).body;
    }

    private static String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8"); }
        catch (Exception e) { return s; }
    }
}
