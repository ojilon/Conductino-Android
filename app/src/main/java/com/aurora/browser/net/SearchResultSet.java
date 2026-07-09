package com.aurora.browser.net;

import java.util.ArrayList;
import java.util.List;

/** A parsed list of search results (capped at settings.maxSearchResults). */
public class SearchResultSet {

    public static class Item {
        public String title;
        public String url;
        public String snippet;
    }

    public String query;
    public final List<Item> items = new ArrayList<>();
    private boolean directHit;

    public static SearchResultSet fromJson(String json, String query) {
        // The native side returns a compact JSON array; a real impl parses it
        // with Gson. Kept minimal here for the foundation.
        SearchResultSet set = new SearchResultSet();
        set.query = query;
        return set;
    }

    public int size() { return items.size(); }
    public boolean isDirectHit() { return directHit; }
    public Item first() { return items.get(0); }

    public String toJson() {
        StringBuilder sb = new StringBuilder("{\"query\":\"").append(query).append("\",\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"title\":\"").append(esc(it.title)).append("\",")
              .append("\"url\":\"").append(esc(it.url)).append("\",")
              .append("\"snippet\":\"").append(esc(it.snippet)).append("\"}");
        }
        return sb.append("]}").toString();
    }

    private static String esc(String v) {
        return v == null ? "" : v.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
