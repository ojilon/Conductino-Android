package com.aurora.browser.state;

/**
 * Every top-level UI the browser can show. Each maps to a folder under
 * assets/ui/<dir>/ containing its own index.html + style.css + logic.js.
 * The frontend is state-driven: Java swaps the whole document per state.
 */
public enum BrowserState {
    WELCOME   ("welcome/index.html"),   // launch / new tab
    SEARCH    ("search/index.html"),    // URL bar focused + engine picker
    RESULTS   ("results/index.html"),   // many search results (limit 300)
    DOCUMENT  ("document/index.html"),  // rendering a single website/doc
    DEVTOOLS  ("devtools/index.html"),  // inspector / network / console
    SETTINGS  ("settings/index.html"),  // preferences UI
    ERROR     ("error/index.html");     // offline / failed load

    private final String indexPath;

    BrowserState(String indexPath) {
        this.indexPath = indexPath;
    }

    public String indexPath() {
        return indexPath;
    }
}
