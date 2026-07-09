package com.aurora.browser.net;

/** Result of a document fetch, plus JSON serializers for the frontend. */
public class FetchResult {
    public String url;
    public boolean ok;
    public int status;
    public String mimeType;
    public String body = "";
    public String error;

    public static FetchResult error(String url, String msg) {
        FetchResult r = new FetchResult();
        r.url = url; r.ok = false; r.status = -1; r.error = msg;
        return r;
    }

    public String toJson() {
        return "{\"url\":" + q(url)
                + ",\"status\":" + status
                + ",\"mime\":" + q(mimeType)
                + ",\"html\":" + q(body) + "}";
    }

    public String toMediaJson() {
        return "{\"url\":" + q(url) + ",\"mime\":" + q(mimeType) + ",\"media\":true}";
    }

    public String errorJson() {
        return "{\"url\":" + q(url) + ",\"status\":" + status + ",\"error\":" + q(error) + "}";
    }

    private static String q(String v) {
        if (v == null) return "null";
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "") + "\"";
    }
}
