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

## 2. Tabs & sessions (next real work)

Goal: each tab is an isolated session (URL history, scroll, form data later).

Suggested shape (C++ preferred for the heavy parts):

```text
struct TabSession {
  uint64_t id;
  std::string current_url;
  std::vector<std::string> history;  // or a ring buffer
  // future: scroll_y, title, favicon hash, last_visit
};

namespace tabs {
  TabSession* create();
  void close(uint64_t id);
  TabSession* active();
  void switch_to(uint64_t id);
  // list, persist, restore
}
```

- Java only holds the active WebView and calls into JNI.
- New Tab button already routes to welcome; replace the stub with `tabs::create()` + load welcome for that session.
- Persistence: simple SQLite or a small binary log under app private storage (see Storage section).

Do **not** put full tab logic inside `BrowserActivity`.

---

## 3. Search engines

Already present: `assets/config/search_engines.json`

- Default: DuckDuckGo
- Also: Google, Brave Search
- Add more or a custom engine by extending the JSON + Settings UI.

`NavigationController` / SettingsManager should read this file. Omnibox and the welcome form both call the same submit path so the chosen engine is always used.

Custom engine example:

```json
{
  "id": "custom",
  "name": "My Engine",
  "queryUrl": "https://example.com/search?q={query}"
}
```

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

## 8. C++ layout (planned move)

Current code lives under `app/src/main/cpp/`. Longer term:

```text
backend/                 ← root-level, not inside app/
  CMakeLists.txt
  include/
  core/                  ← tabs, storage, settings glue
  features/
    text/
    pdf/
    net/
    media/
    ...
  third_party/           ← external libs (gitignored or submodule)
```

Rules you asked for:

- C++17/20/23, **no classes** – prefer structs, enums, free functions, namespaces.
- Experienced-beginner level: short functions, clear ownership.
- CMake must set the standard and expose include paths so `clangd` / LSP works.
- Put long or complex algorithms in `.md` explanations first; implement only the thin stubs until needed.

Suggested starter libraries (document only; do not vendor yet unless required):

| Need              | Library ideas                          |
|-------------------|----------------------------------------|
| HTTP              | libcurl / existing Android stack       |
| JSON              | nlohmann/json or cJSON                 |
| SQLite            | sqlite3 amalgamation                   |
| PDF               | PDFium / mupdf (heavy – plan carefully)|
| Text / HTML       | gumbo / lexbor, or keep simple regex   |
| Crypto            | already have a crypto feature stub     |

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

**Grows (add modules, don’t rewrite shell):**

- Tab / session manager
- Bookmark / history / downloads stores
- Document reader UI + C++ extractors
- AI agent harness (separate surface)
- Media playback helpers (document libraries in feature READMEs)

---

## 11. Short checklist for new features

1. Prefer a new `assets/ui/<feature>/` or a new Java package under `features/`.
2. Wire one sidebar entry or one omnibox command.
3. Put any >~100-line algorithm behind a C++ namespace + short JNI surface.
4. Update this file with the new module name and one-line purpose.

---

*Last updated with the welcome polish and native chrome foundation on branch `front_end`.*
