package com.conductino.study.state;

/**
 * Every top-level UI the browser can show. Each maps to a folder under
 * assets/ui/<dir>/ containing its own index.html (+ optional css/js).
 */
public enum BrowserState {
    WELCOME   ("welcome/index.html"),
    SEARCH    ("search/index.html"),
    RESULTS   ("results/index.html"),
    DOCUMENT  ("document/index.html"),
    DEVTOOLS  ("devtools/index.html"),
    SETTINGS  ("settings/index.html"),
    DOWNLOADS ("downloads/index.html"),
    ERROR     ("error/index.html"),
    EXTERNAL  ("");   // remote WebView load; no local index

    private final String indexPath;

    BrowserState(String indexPath) {
        this.indexPath = indexPath;
    }

    public String indexPath() {
        return indexPath;
    }
}
