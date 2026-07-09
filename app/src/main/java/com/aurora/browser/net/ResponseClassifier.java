package com.aurora.browser.net;

/**
 * Looks at a fetched response and decides which UI state should render it.
 * This is the "Java decides the response" logic: html doc vs media vs error.
 */
public final class ResponseClassifier {

    private ResponseClassifier() {}

    public static ResponseType classify(FetchResult res) {
        if (res == null || !res.ok) return ResponseType.ERROR;
        String mime = res.mimeType == null ? "" : res.mimeType.toLowerCase();
        if (mime.startsWith("text/html") || mime.startsWith("application/xhtml")) {
            return ResponseType.HTML_DOCUMENT;
        }
        if (mime.startsWith("image/") || mime.startsWith("video/") || mime.startsWith("audio/")) {
            return ResponseType.MEDIA;
        }
        if (mime.startsWith("application/pdf")) {
            return ResponseType.MEDIA; // routed to the (future) pdf viewer feature
        }
        return ResponseType.HTML_DOCUMENT;
    }
}
