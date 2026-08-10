package com.conductino.study.library;

import android.content.Context;

import com.conductino.study.logging.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Visited pages. JSON-backed so it works without the native SQLite core.
 * When BUILD_AURORA_CORE is ON you can mirror writes to aurora_add_history.
 */
public final class HistoryStore {

    private static final HistoryStore INSTANCE = new HistoryStore();
    public static HistoryStore get() { return INSTANCE; }

    private static final String FILE = "history.json";
    private static final int MAX = 500;

    private final AtomicLong nextId = new AtomicLong(1);
    private final List<Entry> entries = new ArrayList<>();
    private File file;
    private boolean loaded;

    public static final class Entry {
        public final long id;
        public final String url;
        public final String title;
        public final long visitedAt;

        public Entry(long id, String url, String title, long visitedAt) {
            this.id = id;
            this.url = url;
            this.title = title != null ? title : "";
            this.visitedAt = visitedAt;
        }
    }

    private HistoryStore() {}

    public synchronized void init(Context ctx) {
        if (loaded) return;
        file = new File(ctx.getFilesDir(), FILE);
        load();
        loaded = true;
        LogManager.i("History", "ready count=" + entries.size());
    }

    public synchronized void add(String url, String title) {
        if (url == null || url.isEmpty()) return;
        if (url.contains("appassets.androidplatform.net")) return; // skip local UI

        // Collapse consecutive same URL
        if (!entries.isEmpty() && entries.get(0).url.equals(url)) {
            Entry old = entries.remove(0);
            entries.add(0, new Entry(old.id, url,
                    (title != null && !title.isEmpty()) ? title : old.title,
                    System.currentTimeMillis()));
        } else {
            long id = nextId.getAndIncrement();
            entries.add(0, new Entry(id, url, title, System.currentTimeMillis()));
        }
        while (entries.size() > MAX) {
            entries.remove(entries.size() - 1);
        }
        save();
    }

    public synchronized List<Entry> list(int limit) {
        int n = Math.min(Math.max(limit, 0), entries.size());
        return Collections.unmodifiableList(new ArrayList<>(entries.subList(0, n)));
    }

    public synchronized String listAsJson(int limit) {
        try {
            JSONArray arr = new JSONArray();
            for (Entry e : list(limit)) {
                JSONObject o = new JSONObject();
                o.put("id", e.id);
                o.put("url", e.url);
                o.put("title", e.title);
                o.put("visitedAt", e.visitedAt);
                arr.put(o);
            }
            return arr.toString();
        } catch (Exception ex) {
            return "[]";
        }
    }

    public synchronized void clear() {
        entries.clear();
        save();
    }

    private void load() {
        entries.clear();
        if (file == null || !file.exists()) return;
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[(int) file.length()];
            int n = in.read(buf);
            JSONArray arr = new JSONArray(new String(buf, 0, Math.max(n, 0), StandardCharsets.UTF_8));
            long maxId = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                long id = o.optLong("id", i + 1);
                maxId = Math.max(maxId, id);
                entries.add(new Entry(id, o.optString("url"), o.optString("title"), o.optLong("visitedAt")));
            }
            nextId.set(maxId + 1);
        } catch (Exception e) {
            LogManager.e("History", "load failed", e);
        }
    }

    private void save() {
        if (file == null) return;
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(listAsJson(MAX).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LogManager.e("History", "save failed", e);
        }
    }
}
