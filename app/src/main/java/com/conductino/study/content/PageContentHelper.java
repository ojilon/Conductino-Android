package com.conductino.study.content;

import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.conductino.study.logging.LogManager;

/**
 * Thin helpers for interacting with the loaded page inside WebView.
 *
 * v0:
 *   - findInPage / clearFind  → WebView.findAllAsync
 *   - extractBodyText         → evaluateJavascript document.body.innerText
 *
 * Extend: inject Readability, selection bridge, highlight → AI summarise.
 * Heavy parsing belongs in backend/features/text — not here.
 */
public final class PageContentHelper {

    private PageContentHelper() {}

    public static void findInPage(WebView webView, String query) {
        if (webView == null) return;
        String q = query != null ? query : "";
        webView.findAllAsync(q);
        LogManager.i("Content", "findAllAsync q=" + q);
    }

    public static void clearFind(WebView webView) {
        if (webView == null) return;
        webView.findAllAsync("");
        try {
            webView.clearMatches();
        } catch (Throwable ignored) {
            // older APIs
        }
    }

    public static void findNext(WebView webView, boolean forward) {
        if (webView == null) return;
        webView.findNext(forward);
    }

    /**
     * Extract visible text. Callback receives plain text (may be empty).
     * JSON-string escaped by evaluateJavascript — we strip surrounding quotes lightly.
     */
    public static void extractBodyText(WebView webView, ValueCallback<String> callback) {
        if (webView == null) {
            if (callback != null) callback.onReceiveValue("");
            return;
        }
        String js = "(function(){try{return document.body?document.body.innerText:''}catch(e){return ''}})()";
        webView.evaluateJavascript(js, value -> {
            String text = unwrapJsString(value);
            LogManager.i("Content", "extractBodyText chars=" + text.length());
            if (callback != null) callback.onReceiveValue(text);
        });
    }

    /**
     * Build a minimal JSON payload for the DOCUMENT / reader surface.
     * title + text; you can add url, selection, highlights later.
     */
    public static String readerPayloadJson(String title, String url, String bodyText) {
        String t = jsonEscape(title != null ? title : "");
        String u = jsonEscape(url != null ? url : "");
        String b = jsonEscape(bodyText != null ? bodyText : "");
        return "{\"title\":\"" + t + "\",\"url\":\"" + u + "\",\"text\":\"" + b + "\"}";
    }

    private static String unwrapJsString(String value) {
        if (value == null || "null".equals(value)) return "";
        String s = value;
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String jsonEscape(String in) {
        StringBuilder sb = new StringBuilder(in.length() + 16);
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}
