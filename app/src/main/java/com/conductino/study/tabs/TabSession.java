package com.conductino.study.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One browser tab / session.
 *
 * Keep this a plain data holder. Persistence, favicons, scroll restore, etc.
 * belong in TabManager or the C backend later — not here.
 */
public final class TabSession {

    public final long id;
    public String currentUrl;
    public String title;
    private final List<String> history;

    TabSession(long id) {
        this.id = id;
        this.currentUrl = "";
        this.title = "New Tab";
        this.history = new ArrayList<>();
    }

    public void setUrl(String url) {
        if (url == null) url = "";
        this.currentUrl = url;
        if (!url.isEmpty()) {
            // Avoid consecutive duplicates
            if (history.isEmpty() || !history.get(history.size() - 1).equals(url)) {
                history.add(url);
            }
        }
    }

    public List<String> historyView() {
        return Collections.unmodifiableList(history);
    }

    public boolean isEmpty() {
        return currentUrl == null || currentUrl.isEmpty();
    }
}
