package com.conductino.study.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.conductino.study.logging.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads config/settings.json + config/search_engines.json.
 * Selected search engine is persisted in SharedPreferences so the user can
 * change it from Settings without rewriting assets.
 *
 * Extend: add custom engines to search_engines.json or append at runtime
 * via addCustomEngine() (below) and persist a mutable copy under filesDir.
 */
public class SettingsManager {

    private static final SettingsManager INSTANCE = new SettingsManager();
    public static SettingsManager get() { return INSTANCE; }
    private SettingsManager() {}

    private static final String PREFS = "conductino_settings";
    private static final String KEY_ENGINE = "search_engine_id";

    private JSONObject settings = new JSONObject();
    private JSONObject engines = new JSONObject();
    private SharedPreferences prefs;
    private String selectedEngineId;

    public void load(Context ctx) {
        settings = readAsset(ctx, "config/settings.json");
        engines  = readAsset(ctx, "config/search_engines.json");
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        String fromJson = engines.optString("default", "duckduckgo");
        selectedEngineId = prefs.getString(KEY_ENGINE, fromJson);
        // Validate id still exists
        if (engineQueryUrl(selectedEngineId).isEmpty()) {
            selectedEngineId = fromJson;
        }
        LogManager.i("Settings", "loaded config, engine=" + selectedEngineId);
    }

    public int maxSearchResults() {
        JSONObject g = settings.optJSONObject("general");
        return g != null ? g.optInt("maxSearchResults", 300) : 300;
    }

    public String userAgent() {
        JSONObject net = settings.optJSONObject("network");
        return net != null
                ? net.optString("userAgent", "AuroraBrowser/0.1")
                : "AuroraBrowser/0.1";
    }

    /** Currently selected engine id (persisted). */
    public String defaultEngine() {
        return selectedEngineId != null ? selectedEngineId : "duckduckgo";
    }

    public void setDefaultEngine(String id) {
        if (id == null || engineQueryUrl(id).isEmpty()) {
            LogManager.i("Settings", "ignore unknown engine id=" + id);
            return;
        }
        selectedEngineId = id;
        if (prefs != null) {
            prefs.edit().putString(KEY_ENGINE, id).apply();
        }
        LogManager.i("Settings", "search engine -> " + id);
    }

    public String engineQueryUrl(String id) {
        return engineField(id, "queryUrl");
    }

    public String engineSuggestUrl(String id) {
        return engineField(id, "suggestUrl");
    }

    public String engineResultSelector(String id) {
        return engineField(id, "resultSelector");
    }

    public String engineName(String id) {
        String name = engineField(id, "name");
        return name.isEmpty() ? id : name;
    }

    /** Build a full search URL for the given query using the selected engine. */
    public String buildSearchUrl(String query) {
        return buildSearchUrl(defaultEngine(), query);
    }

    public String buildSearchUrl(String engineId, String query) {
        String template = engineQueryUrl(engineId);
        if (template.isEmpty()) {
            template = "https://duckduckgo.com/html/?q={query}";
        }
        String encoded;
        try {
            encoded = URLEncoder.encode(query == null ? "" : query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encoded = query != null ? query.replace(" ", "+") : "";
        }
        return template.replace("{query}", encoded);
    }

    /** Lightweight list for Settings UI / engine picker. */
    public List<EngineInfo> listEngines() {
        List<EngineInfo> out = new ArrayList<>();
        JSONArray arr = engines.optJSONArray("engines");
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.optJSONObject(i);
            if (e == null) continue;
            String id = e.optString("id", "");
            if (id.isEmpty()) continue;
            out.add(new EngineInfo(id, e.optString("name", id), e.optString("queryUrl", "")));
        }
        return out;
    }

    public static final class EngineInfo {
        public final String id;
        public final String name;
        public final String queryUrl;

        public EngineInfo(String id, String name, String queryUrl) {
            this.id = id;
            this.name = name;
            this.queryUrl = queryUrl;
        }
    }

    private String engineField(String id, String field) {
        JSONArray arr = engines.optJSONArray("engines");
        if (arr == null) return "";
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.optJSONObject(i);
            if (e != null && id.equals(e.optString("id"))) {
                return e.optString(field, "");
            }
        }
        return "";
    }

    private JSONObject readAsset(Context ctx, String path) {
        try (InputStream is = ctx.getAssets().open(path)) {
            byte[] buf = new byte[is.available()];
            int read = is.read(buf);
            return new JSONObject(new String(buf, 0, Math.max(read, 0), "UTF-8"));
        } catch (Exception e) {
            LogManager.e("Settings", "failed reading " + path, e);
            return new JSONObject();
        }
    }
}
