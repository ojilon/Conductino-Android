package com.conductino.study.downloads;

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
 * Internal downloads registry + files under filesDir/downloads/.
 *
 * v0: JSON index on disk; files stay private until you implement export.
 * Extend: queue, progress callbacks, resume, export to MediaStore/Downloads.
 */
public final class DownloadStore {

    private static final DownloadStore INSTANCE = new DownloadStore();
    public static DownloadStore get() { return INSTANCE; }

    private static final String DIR_NAME = "downloads";
    private static final String INDEX_NAME = "index.json";

    private final AtomicLong nextId = new AtomicLong(1);
    private final List<DownloadRecord> records = new ArrayList<>();
    private File downloadsDir;
    private File indexFile;
    private boolean loaded;

    private DownloadStore() {}

    public synchronized void init(Context ctx) {
        if (loaded) return;
        downloadsDir = new File(ctx.getFilesDir(), DIR_NAME);
        if (!downloadsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            downloadsDir.mkdirs();
        }
        indexFile = new File(downloadsDir, INDEX_NAME);
        loadIndex();
        loaded = true;
        LogManager.i("Downloads", "store ready path=" + downloadsDir.getAbsolutePath()
                + " count=" + records.size());
    }

    public File getDownloadsDir() {
        return downloadsDir;
    }

    public synchronized List<DownloadRecord> list() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    public synchronized int count() {
        return records.size();
    }

    /**
     * Register a file already written under the downloads directory
     * (e.g. by WebView download listener or OkHttp).
     */
    public synchronized DownloadRecord add(String url, String fileName, File localFile,
                                           String mimeType) {
        long id = nextId.getAndIncrement();
        long bytes = localFile != null && localFile.exists() ? localFile.length() : 0;
        String path = localFile != null ? localFile.getAbsolutePath() : "";
        DownloadRecord rec = new DownloadRecord(
                id, url != null ? url : "", fileName != null ? fileName : "file",
                path, bytes, System.currentTimeMillis(), mimeType);
        records.add(0, rec); // newest first
        saveIndex();
        LogManager.i("Downloads", "added id=" + id + " name=" + rec.fileName);
        return rec;
    }

    /** Suggest a unique file path under the internal downloads dir. */
    public synchronized File allocateFile(String suggestedName) {
        String safe = sanitize(suggestedName);
        File target = new File(downloadsDir, safe);
        if (!target.exists()) return target;
        int i = 1;
        int dot = safe.lastIndexOf('.');
        String base = dot > 0 ? safe.substring(0, dot) : safe;
        String ext = dot > 0 ? safe.substring(dot) : "";
        while (target.exists()) {
            target = new File(downloadsDir, base + "_" + i + ext);
            i++;
        }
        return target;
    }

    public synchronized String listAsJson() {
        try {
            JSONArray arr = new JSONArray();
            for (DownloadRecord r : records) {
                JSONObject o = new JSONObject();
                o.put("id", r.id);
                o.put("url", r.url);
                o.put("fileName", r.fileName);
                o.put("localPath", r.localPath);
                o.put("bytes", r.bytes);
                o.put("createdAt", r.createdAt);
                o.put("mimeType", r.mimeType);
                arr.put(o);
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private void loadIndex() {
        records.clear();
        if (indexFile == null || !indexFile.exists()) return;
        try (FileInputStream in = new FileInputStream(indexFile)) {
            byte[] buf = new byte[(int) indexFile.length()];
            int n = in.read(buf);
            JSONArray arr = new JSONArray(new String(buf, 0, Math.max(n, 0), StandardCharsets.UTF_8));
            long maxId = 0;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                long id = o.optLong("id", i + 1);
                maxId = Math.max(maxId, id);
                records.add(new DownloadRecord(
                        id,
                        o.optString("url", ""),
                        o.optString("fileName", "file"),
                        o.optString("localPath", ""),
                        o.optLong("bytes", 0),
                        o.optLong("createdAt", 0),
                        o.optString("mimeType", "application/octet-stream")
                ));
            }
            nextId.set(maxId + 1);
        } catch (Exception e) {
            LogManager.e("Downloads", "load index failed", e);
        }
    }

    private void saveIndex() {
        if (indexFile == null) return;
        try (FileOutputStream out = new FileOutputStream(indexFile)) {
            out.write(listAsJson().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LogManager.e("Downloads", "save index failed", e);
        }
    }

    private static String sanitize(String name) {
        if (name == null || name.trim().isEmpty()) return "download.bin";
        String s = name.trim().replaceAll("[\\/:*?\"<>|]", "_");
        if (s.length() > 120) s = s.substring(0, 120);
        return s;
    }
}
