# Conductino – Foundation Map

Keep the chrome stable. Prefer new modules and short stubs over rewriting the shell.

---

## 1. Layout — in place

DrawerLayout → top bar (Home | New Tab | Omnibox | Menu) + progress + WebView + end drawer.

---

## 2. Tabs — **IMPLEMENTED (v0)**

`tabs/TabManager`, `TabSession`. Guide: `tabs/README.md`.

---

## 3. Search engines — **IMPLEMENTED (v0)**

`SettingsManager.buildSearchUrl`, Settings picker, SharedPreferences. Guide: `settings/README.md`.

---

## 4. Sidebar — **IMPLEMENTED (v0)**

Chrome states: New Tab, History, Bookmarks, Downloads, Settings.  
Page states: Refresh, Find in page, Bookmark, History, Downloads, Reader.

---

## 5. Downloads — **IMPLEMENTED (v0)**

`downloads/DownloadStore`, WebView download listener, `assets/ui/downloads/`. Guide: `downloads/README.md`.

---

## 6. Bookmarks & history — **IMPLEMENTED (v0)**

| Piece | Location |
|-------|----------|
| History | `library/HistoryStore.java` → `filesDir/history.json` |
| Bookmarks | `library/BookmarkStore.java` → `filesDir/bookmarks.json` |
| Guide | `library/README.md` |
| UI | `assets/ui/history/`, `assets/ui/bookmarks/` |
| States | `BrowserState.HISTORY`, `BOOKMARKS` |

**Behaviour**

- History: written on non-local URL updates (max 500, consecutive duplicates collapsed).
- Bookmark: sidebar on a page → `BookmarkStore.add(url, title)`.
- List UIs open from sidebar; tap a row → `Aurora.open(url)`.

**Extend:** folders, search, native SQLite mirror via `NativeCore.addHistory`.

---

## 7. Page content interaction

Find in page, extract text, reader mode, AI — next step.

---

## 8. C++ — **MOVED** to `backend/`

---

## 9. Themes

`theme.css` + `res/values` colors/themes.

---

## 10–11. Stable APIs & checklist

Stable: layout IDs, StateManager, TabManager, SettingsManager engines, DownloadStore, HistoryStore, BookmarkStore, Aurora bridge.

*Last updated: §6 done. Next: §7 content interaction, then §9 themes.*
