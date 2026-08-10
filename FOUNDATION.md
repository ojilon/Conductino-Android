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

- **Welcome / Settings / Error / Document UIs** live under `app/src/main/assets/ui/<name>/` and are loaded into the same WebView.
- Native chrome stays in XML + `BrowserActivity`. Dynamic surfaces stay in HTML/CSS/JS.

---

## 2. Tabs & sessions — **IMPLEMENTED (v0)**

**Status:** Working in-memory sessions. Extend, do not replace.

| Piece | Location |
|-------|----------|
| Data | `app/.../tabs/TabSession.java` |
| Manager | `app/.../tabs/TabManager.java` |
| Guide | `app/.../tabs/README.md` |
| C sketch | `backend/include/tabs.h` + `backend/features/tabs/README.md` |

**Behaviour now**

- App always has ≥1 tab (`TabManager` creates one on first use).
- **New Tab** → `TabManager.create()` then welcome.
- **Home** → same tab, welcome (clears session URL/title).
- External page loads call `recordNavigation(url, title)` from the WebView UI callback.

**Your next extensions**

1. Tab switcher UI using `list()` / `switchTo(id)` then reload `active().currentUrl`.
2. Persist sessions (SQLite) — see tabs README.
3. Optional C mirror via `tabs.h` when you need native persistence.

Do **not** put tab lists inside `BrowserActivity` beyond the thin wiring already there.

---

## 3. Search engines — **IMPLEMENTED (v0)**

**Status:** Omnibox + welcome use the selected engine; choice persists.

| Piece | Location |
|-------|----------|
| Catalog | `assets/config/search_engines.json` (DuckDuckGo, Google, Brave) |
| API | `SettingsManager.buildSearchUrl()` / `setDefaultEngine()` / `listEngines()` |
| Nav | `NavigationController.handleInput()` uses `buildSearchUrl` for queries |
| UI | `assets/ui/settings/index.html` engine picker → `Aurora.selectEngine` |
| Guide | `app/.../settings/README.md` |

**Behaviour now**

- Selected engine id in SharedPreferences (`conductino_settings`).
- Non-URL input → `queryUrl` template with `{query}` URL-encoded.
- Settings lists DuckDuckGo / Google / Brave; tap persists and applies immediately.

**Extend**

- Add engines in JSON (see settings README).
- Custom runtime engines: mutable copy under `filesDir`.
- Typeahead via `suggestUrl` (still returns `[]`).

---

## 4. Sidebar contract

`BrowserActivity.populateSidebarOptions()` already branches on `BrowserState`:

| State     | Options (current stubs)              |
|-----------|--------------------------------------|
| WELCOME   | New Tab, History, Downloads, Settings |
| PAGE / other | Refresh, Find in page, Bookmark, Reader |

Add new options only inside that method (or a small helper). Keep button creation in `addSidebarOption`.

---

## 5. Downloads

Two layers:

1. **Internal store** (app-private) – always available, no permission drama.
2. **Export** to the public Downloads folder when the user chooses (needs the storage / media permissions already requested).

Suggested C++ / Java split:

- C++ or Java: queue, progress, file naming, resume.
- Java: `DownloadManager` or OkHttp + file write; notify UI.
- Sidebar “Downloads” opens a simple list (can be another `assets/ui/downloads/` surface).

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

WebView is the host; you are **not** limited to the sandbox if you inject JS and bridge carefully.

Useful entry points (already usable):

| Goal                    | Approach                                              |
|-------------------------|-------------------------------------------------------|
| Find in page            | `WebView.findAllAsync(query)` + `setFindListener`     |
| Extract text / main     | Inject JS (`document.body.innerText` or Readability)  |
| Highlight / selection   | JS selection API → bridge to Java → C++ / AI          |
| PDF / document view     | Separate UI surface (`assets/ui/document/`) + native PDF lib or WebView PDF |
| Summarise / cite        | Extract → send to your AI backend; keep UI thin       |

Keep heavy parsing in C++ (see `features/text`, `features/pdf` stubs). Java only orchestrates.

---

## 8. C++ layout — **MOVED**

Native code lives at repo root:

```text
backend/
  CMakeLists.txt
  include/
  src/
  features/
  third_party/   ← gitignored; place curl/sqlite/lexbor locally
  cmake/CompilerFlags.cmake  ← C++23 ready
```

`app/build.gradle` points at `../backend/CMakeLists.txt`.  
`BUILD_AURORA_CORE` defaults OFF until third_party is present.

Rules: structs / free functions / namespaces; no classes; short functions.

---

## 9. Themes

- Tokens live in `assets/ui/shared/theme.css` and `res/values/colors.xml` / `themes.xml`.
- Prefer CSS variables for HTML surfaces; native chrome uses the XML colors.
- Settings can later write a small override (e.g. force dark / accent) that both sides read.

---

## 10. What stays stable vs what grows

**Stable (change rarely):**

- `activity_browser.xml` structure
- Top-bar IDs and drawer contract
- `WebViewHost` + `StateManager` + `BrowserState` enum
- Aurora JS bridge contract
- `TabManager` public API (create / close / switchTo / active)
- `SettingsManager.buildSearchUrl` / engine id persistence

**Grows (add modules, don’t rewrite shell):**

- Tab switcher UI + persistence
- Bookmark / history / downloads stores
- Document reader UI + C++ extractors
- AI agent harness (separate surface)
- Media playback helpers

---

## 11. Short checklist for new features

1. Prefer a new `assets/ui/<feature>/` or a new Java package under `features/` / `tabs/`.
2. Wire one sidebar entry or one omnibox command.
3. Put any >~100-line algorithm behind a C++ namespace + short JNI surface.
4. Update this file with the new module name and one-line purpose.

---

*Last updated: §3 Search engines implemented. Next: §4 Sidebar (document only if already OK) then §5 Downloads.*
