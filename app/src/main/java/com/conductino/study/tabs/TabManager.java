package com.conductino.study.tabs;

import com.conductino.study.logging.LogManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory tab sessions.
 *
 * v0: pure Java, single active WebView (the one in BrowserActivity).
 * Later: call into backend C for persistence / multi-process isolation.
 *
 * Contract:
 *   create()     → new empty session, becomes active
 *   close(id)    → remove; if active, switch to another or create one
 *   switchTo(id) → change active pointer (UI reloads that session's URL)
 *   active()     → current TabSession
 *
 * BrowserActivity should only talk to this class for tab lifecycle.
 */
public final class TabManager {

    private static final TabManager INSTANCE = new TabManager();
    public static TabManager get() { return INSTANCE; }

    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, TabSession> tabs = new LinkedHashMap<>();
    private long activeId = -1;

    private TabManager() {
        // Ensure there is always at least one tab
        create();
    }

    public synchronized TabSession create() {
        long id = nextId.getAndIncrement();
        TabSession session = new TabSession(id);
        tabs.put(id, session);
        activeId = id;
        LogManager.i("TabManager", "created tab id=" + id + " count=" + tabs.size());
        return session;
    }

    public synchronized void close(long id) {
        if (!tabs.containsKey(id)) return;
        tabs.remove(id);
        LogManager.i("TabManager", "closed tab id=" + id + " count=" + tabs.size());

        if (tabs.isEmpty()) {
            create();
            return;
        }
        if (activeId == id) {
            // Pick the last remaining tab
            activeId = -1;
            for (Long k : tabs.keySet()) {
                activeId = k;
            }
        }
    }

    public synchronized boolean switchTo(long id) {
        if (!tabs.containsKey(id)) return false;
        activeId = id;
        LogManager.i("TabManager", "switched to tab id=" + id);
        return true;
    }

    public synchronized TabSession active() {
        TabSession s = tabs.get(activeId);
        if (s == null) {
            return create();
        }
        return s;
    }

    public synchronized List<TabSession> list() {
        return new ArrayList<>(tabs.values());
    }

    public synchronized int count() {
        return tabs.size();
    }

    /** Record navigation on the active tab. */
    public synchronized void recordNavigation(String url, String title) {
        TabSession s = active();
        s.setUrl(url);
        if (title != null && !title.isEmpty()) {
            s.title = title;
        }
    }
}
