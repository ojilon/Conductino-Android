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

/** Saved pages the user wants to revisit. JSON under filesDir. */
public final class BookmarkStore {

    private static final BookmarkStore INSTANCE = new BookmarkStore();
    public static BookmarkStore get() { return INSTANCE; }

    private static final String FILE = "bookmarks.json";

    private final AtomicLong nextId = new AtomicLong(1);
    private final List<Entry> entries = new ArrayList<>();
    private File file;
    private boolean loaded;

    public static final class Entry {
        public final long id;
        public final String url;
        public final String title;
        public final long createdAt;

        public Entry(long id, String url, String title, long createdAt) {
            this.id = id;
            this.url = url;
            this.title = title != null ? title : "";
            this.createdAt = createdAt;
        }
    }

    private BookmarkStore() {}

    public synchronized void init(Context ctx) {
        if (loaded) return;
        file = new File(ctx.getFilesDir(), FILE);
        load();
        loaded = true;
        LogManager.i("Bookmarks", "ready count=" + entries.size());
    }

    /** @return true if added, false if duplicate URL */
    public synchronized boolean add(String url, String title) {
        if (url == null || url.isEmpty()) return false;
        for (Entry e : entries) {
            if (e.url.equals(url)) return false;
        }
        long id = nextId.getAndIncrement();
        entries.add(0, new Entry(id, url, title, System.currentTimeMillis()));
        save();
        LogManager.i("Bookmarks", "added " + url);
        return true;
    }

    public synchronized boolean remove(long id) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id == id) {
                entries.remove(i);
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized List<Entry> list() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized String listAsJson() {
        try {
            JSONArray arr = new JSONArray();
            for (Entry e : entries) {
                JSONObject o = new JSONObject();
                o.put("id", e.id);
                o.put("url", e.url);
                o.put("title", e.title);
                o.put("createdAt", e.createdAt);
                arr.put(o);
            }
            return arr.toString();
        } catch (Exception ex) {
            return "[]";
        }
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
                entries.add(new Entry(id, o.optString("url"), o.optString("title"), o.optLong("createdAt")));
            }
            nextId.set(maxId + 1);
        } catch (Exception e) {
            LogManager.e("Bookmarks", "load failed", e);
        }
    }

    private void save() {
        if (file == null) return;
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(listAsJson().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LogManager.e("Bookmarks", "save failed", e);
        }
    }
}
