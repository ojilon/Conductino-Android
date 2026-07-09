package com.aurora.browser.settings;

import android.content.Context;

import com.aurora.browser.logging.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Loads and exposes config/settings.json + config/search_engines.json.
 * A real build swaps assets defaults with a mutable copy in filesDir so the
 * settings UI can persist changes.
 */
public class SettingsManager {

    private static final SettingsManager INSTANCE = new SettingsManager();
    public static SettingsManager get() { return INSTANCE; }
    private SettingsManager() {}

    private JSONObject settings = new JSONObject();
    private JSONObject engines = new JSONObject();

    public void load(Context ctx) {
        settings = readAsset(ctx, "config/settings.json");
        engines  = readAsset(ctx, "config/search_engines.json");
        LogManager.i("Settings", "loaded config, engine=" + defaultEngine());
    }

    public int maxSearchResults() {
        return settings.optJSONObject("general") != null
                ? settings.optJSONObject("general").optInt("maxSearchResults", 300) : 300;
    }

    public String userAgent() {
        JSONObject net = settings.optJSONObject("network");
        return net != null ? net.optString("userAgent", "AuroraBrowser/0.1") : "AuroraBrowser/0.1";
    }

    public String defaultEngine() {
        return engines.optString("default", "duckduckgo");
    }

    public String engineQueryUrl(String id)  { return engineField(id, "queryUrl"); }
    public String engineSuggestUrl(String id){ return engineField(id, "suggestUrl"); }
    public String engineResultSelector(String id){ return engineField(id, "resultSelector"); }

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
            return new JSONObject(new String(buf, 0, read, "UTF-8"));
        } catch (Exception e) {
            LogManager.e("Settings", "failed reading " + path, e);
            return new JSONObject();
        }
    }
}
