# content/ — page interaction foundation

## PageContentHelper

| Method | Uses |
|--------|------|
| `findInPage(webView, query)` | `WebView.findAllAsync` |
| `clearFind` / `findNext` | clear / step matches |
| `extractBodyText` | `evaluateJavascript` → `document.body.innerText` |
| `readerPayloadJson` | JSON for DOCUMENT UI |

## How BrowserActivity uses it

- **Find in page** → simple prompt (or Toast stub) then `findInPage`
- **Reader / Document** → extract text → `StateManager.transitionTo(DOCUMENT, payload)`

## Extend (study tools)

1. **Find UI** — bottom bar with query EditText + prev/next (call `findNext`).
2. **Selection** — inject JS that listens to `selectionchange` and posts to `AuroraNative`.
3. **Readability** — inject a bundled Readability.js, extract article HTML, show in DOCUMENT.
4. **AI** — send extracted text to your backend; keep UI in `assets/ui/`.
5. **C++** — large HTML parse / PDF in `backend/features/text` and `pdf`.

Do not put extraction algorithms inside the Activity.
