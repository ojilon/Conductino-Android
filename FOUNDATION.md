# Conductino – Foundation Map

This document records the intended architecture so the XML/Java/HTML skeleton stays stable while you add study tools, AI helpers, document readers, etc.

Keep changes to the chrome (top bar, drawer, WebView host) minimal. Prefer new modules and short stubs over rewriting the shell.

---

## 1. High-level layout (already in place)

```
DrawerLayout
├── main_content (vertical)
│   ├── native_top_bar   ← Home | New Tab | Omnibox | Menu
│   ├── progress_bar
│   └── WebView          ← local assets/ui/* OR remote pages
└── side_drawer (end)
    └── drawer_options_container  ← filled by BrowserActivity from current state
```

---

## 2. Tabs & sessions — **IMPLEMENTED (v0)**

See `app/.../tabs/` + `tabs/README.md`. New Tab creates a session; Home keeps the same tab.

---

## 3. Search engines — **IMPLEMENTED (v0)**

See `SettingsManager` + `settings/README.md`. Omnibox uses selected engine; Settings picker persists choice.

---

## 4. Sidebar contract — **IMPLEMENTED (v0)**

`BrowserActivity.populateSidebarOptions()` branches on state:

| State group | Options |
|-------------|---------|
| WELCOME / SETTINGS / DOWNLOADS | New Tab, History, Downloads, Settings |
| EXTERNAL / other page | Refresh, Find in page, Bookmark, Downloads, Reader |

Rules:

- Add options only via `addSidebarOption`.
- Keep handlers thin (delegate to managers / StateManager).
- History / Bookmark still open next foundation steps.

---

## 5. Downloads — **IMPLEMENTED (v0)**

| Piece | Location |
|-------|----------|
| Record / store | `downloads/DownloadRecord.java`, `DownloadStore.java` |
| Guide | `downloads/README.md` |
| UI | `assets/ui/downloads/index.html` |
| State | `BrowserState.DOWNLOADS` |
| Capture | `WebViewHost` `DownloadListener` → background fetch → store |

**Behaviour**

- Files land under `filesDir/downloads/` (app-private).
- Index: `filesDir/downloads/index.json`.
- Sidebar **Downloads** → `transitionTo(DOWNLOADS, listAsJson())`.
- Public export to phone Downloads is **not** implemented yet (documented in README).

**Extend:** progress UI, queue, MediaStore export, FileProvider open.

---

## 6. Bookmarks & history

Minimal schema (SQLite or JSON for v0):

```text
bookmarks(id, url, title, created_at, folder?)
history(id, url, title, visited_at)
```

Write from:

- WebViewClient `onPageFinished` → history
- Sidebar “Bookmark” → bookmarks

Later: sync, folders, search.

---

## 7. Interacting with page content (study tools foundation)

| Goal                    | Approach                                              |
|-------------------------|-------------------------------------------------------|
| Find in page            | `WebView.findAllAsync(query)` + `setFindListener`     |
| Extract text / main     | Inject JS (`document.body.innerText` or Readability)  |
| Highlight / selection   | JS selection API → bridge to Java → C++ / AI          |
| PDF / document view     | `assets/ui/document/` + native PDF lib or WebView PDF |
| Summarise / cite        | Extract → AI backend; keep UI thin                    |

---

## 8. C++ layout — **MOVED**

`backend/` at repo root; CMake path from `app/build.gradle`. `BUILD_AURORA_CORE` defaults OFF.

---

## 9. Themes

- Tokens: `assets/ui/shared/theme.css`, `res/values/colors.xml` / `themes.xml`.
- Prefer CSS variables for HTML; XML for native chrome.

---

## 10. Stable vs grows

**Stable:** layout IDs, StateManager, TabManager API, SettingsManager engine API, DownloadStore API, Aurora bridge.

**Grows:** tab switcher, history/bookmarks, export downloads, reader/AI, media.

---

## 11. Checklist

1. New module under package or `assets/ui/<feature>/`.
2. One sidebar or omnibox entry.
3. Heavy logic → C namespace + thin JNI.
4. Update this file.

---

*Last updated: §4 sidebar documented, §5 Downloads v0. Next: §6 Bookmarks & history.*
