# downloads/ — internal store

## What exists

| Class | Role |
|-------|------|
| `DownloadRecord` | id, url, fileName, localPath, bytes, createdAt, mime |
| `DownloadStore` | filesDir/downloads/ + index.json registry |

## Behaviour (v0)

- `DownloadStore.init(context)` from Application or first use.
- WebView `setDownloadListener` writes into `allocateFile()` and calls `add()`.
- Sidebar **Downloads** opens `assets/ui/downloads/` and receives JSON list via payload.
- Files stay **app-private**. No automatic export to public Downloads yet.

## Extend

1. **Progress / queue** — OkHttp or DownloadManager; update a `status` field on the record.
2. **Export** — copy `localPath` to `MediaStore.Downloads` or `Environment.DIRECTORY_DOWNLOADS` after user confirms (needs permission already requested on older APIs).
3. **Open file** — `FileProvider` + intent.
4. **C backend** — optional; keep Java store until you need shared native logic.

## Do not

- Put download lists or file IO inside `BrowserActivity` beyond one sidebar line and the WebView listener wiring.
