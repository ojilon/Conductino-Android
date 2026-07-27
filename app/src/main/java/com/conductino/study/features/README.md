# `java/.../features/` — future Java-side feature packages

Empty on purpose. Each future feature gets its own package here so the core
stays small. Most will be a thin Java coordinator over a `cpp/features/*`
native module + a `assets/ui/*` state.

| Package (create it) | Feature            | Talks to                                  |
|---------------------|--------------------|-------------------------------------------|
| `tabs/`             | Multi-tab manager  | StateManager + a `assets/ui/tabs/` state  |
| `history/`          | History/bookmarks  | `NativeCore.recentHistory` (SQLite in C)  |
| `downloads/`        | Download manager    | net/Fetcher + `cpp/features/archive`     |
| `pdf/`              | PDF viewer          | `cpp/features/pdf` via JNI               |
| `media/`            | Media player        | `cpp/features/media` via JNI             |
| `extensions/`       | Extension host      | new JS bridge surface                     |
| `sync/`             | Account sync        | `cpp/features/crypto`                     |
| `adblock/`          | Content blocking    | net/ResourceRouter filter hook            |

## Existing packages (implemented foundation)
```
api/       JS <-> Java contract (BrowserBridge, window.AuroraNative)
web/       WebView host, WebViewClient (stream intercept), ChromeClient
state/     BrowserState enum + StateManager (which UI to render)
net/       Fetcher, ResourceRouter, classifiers, search client, nav controller
core/      NativeCore — the single JNI gateway into the C backend
settings/  SettingsManager — reads config/*.json
logging/   LogManager — central log stream (feeds DevTools)
```
