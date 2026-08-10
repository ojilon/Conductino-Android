# tabs/ — session foundation

## What exists

| Class         | Role |
|---------------|------|
| `TabSession`  | Data: id, currentUrl, title, in-tab history list |
| `TabManager`  | Singleton: create / close / switchTo / active / list |

## How BrowserActivity uses it

- **New Tab** → `TabManager.get().create()` then load welcome for that session
- **Home** → keep the same tab, load welcome (clear omnibox)
- On page load → `TabManager.get().recordNavigation(url, title)` (wire from WebViewHost callback when ready)

## Extending (your work)

1. **Tab switcher UI**  
   Read `TabManager.list()`, show titles/URLs, call `switchTo(id)`, then tell WebViewHost to load `active().currentUrl` or welcome if empty.

2. **Persistence**  
   On pause/destroy, serialize `list()` to SQLite (see `backend/src/storage.c` or a Java Room/SQLite helper). On boot, restore and `switchTo` last active.

3. **C backend** (optional later)  
   Mirror with:
   ```c
   typedef struct TabSession { uint64_t id; char *url; /* ... */ } TabSession;
   namespace tabs { TabSession *create(void); void close(uint64_t); ... }
   ```
   JNI only for create/close/list; Java keeps the active WebView.

4. **Do not** put tab lists or SQLite inside `BrowserActivity`.

## Limits of v0

- One physical WebView — switching tabs reloads URL; no frozen WebView snapshots yet.
- History is a simple list (no back/forward stack index).
- No favicon / scrollY yet — add fields on `TabSession` when needed.
