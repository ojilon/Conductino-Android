# library/ — history & bookmarks

## Classes

| Class | File | Role |
|-------|------|------|
| `HistoryStore` | `history.json` in filesDir | Visited URLs (max 500, newest first) |
| `BookmarkStore` | `bookmarks.json` | User-saved URLs (unique by URL) |

## Behaviour (v0)

- Initialized from `ConductinoApplication`.
- **History:** recorded when WebView reports a non-local URL (Activity UI callback / page finished).
- **Bookmark:** sidebar on a page → `BookmarkStore.add(currentUrl, title)`.
- UI surfaces: `assets/ui/history/`, `assets/ui/bookmarks/` receive JSON arrays via StateManager payload.

## Extend

1. Folders / tags on bookmarks.
2. Search filter in the HTML UI.
3. When native core is on: also call `NativeCore.addHistory` / SQLite for a single source of truth.
4. Sync / export.

Keep IO out of `BrowserActivity` beyond one-line calls.
