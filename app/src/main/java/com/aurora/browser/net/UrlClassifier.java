package com.aurora.browser.net;

import java.util.regex.Pattern;

/** Cheap heuristics to tell a URL from a search query typed in the omnibox. */
public final class UrlClassifier {

    private static final Pattern HAS_SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://.*");
    private static final Pattern LOOKS_HOST = Pattern.compile(
            "^[\\w-]+(\\.[\\w-]+)+(/.*)?$");   // e.g. example.com, sub.site.io/x

    private UrlClassifier() {}

    public static boolean looksLikeUrl(String text) {
        String t = text.trim();
        if (t.isEmpty() || t.contains(" ")) return false;
        return HAS_SCHEME.matcher(t).matches() || LOOKS_HOST.matcher(t).matches();
    }

    public static String normalize(String text) {
        String t = text.trim();
        if (HAS_SCHEME.matcher(t).matches()) return t;
        return "https://" + t;
    }
}
