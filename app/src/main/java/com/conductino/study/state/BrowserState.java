package com.conductino.study.state;

public enum BrowserState {
    WELCOME   ("welcome/index.html"),
    SEARCH    ("search/index.html"),
    RESULTS   ("results/index.html"),
    DOCUMENT  ("document/index.html"),
    DEVTOOLS  ("devtools/index.html"),
    SETTINGS  ("settings/index.html"),
    DOWNLOADS ("downloads/index.html"),
    HISTORY   ("history/index.html"),
    BOOKMARKS ("bookmarks/index.html"),
    TABS      ("tabs/index.html"),
    ERROR     ("error/index.html"),
    EXTERNAL  ("");

    private final String indexPath;

    BrowserState(String indexPath) {
        this.indexPath = indexPath;
    }

    public String indexPath() {
        return indexPath;
    }
}
