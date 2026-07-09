# `assets/ui/` — State-driven frontend

The browser UI is **not** a single page. Java's `StateManager` loads a whole
different document per browser state into the WebView. Each state gets its own
folder with `index.html` + `style.css` + `logic.js`.

| State      | Folder       | Purpose                                           |
|------------|--------------|---------------------------------------------------|
| WELCOME    | `welcome/`   | New-tab / launch screen, omnibox, shortcuts       |
| SEARCH     | `search/`    | Omnibox focused, engine picker, live suggestions  |
| RESULTS    | `results/`   | Many search results (capped at 300)               |
| DOCUMENT   | `document/`  | Renders a single website or media object          |
| DEVTOOLS   | `devtools/`  | Console / Network / Elements inspector            |
| SETTINGS   | `settings/`  | Preferences (backed by `config/settings.json`)    |
| ERROR      | `error/`     | Offline / failed load                             |

## Contract
* Include `../shared/aurora-api.js`; call `Aurora.ready('<state>')` on load.
* Call INTO Java via `Aurora.submit / open / selectEngine / suggest / devtools`.
* Receive data FROM Java via `Aurora.on('payload', cb)` — Java emits after load.

## Add a new state (future)
1. Create `assets/ui/<state>/` with the three files.
2. Add an enum entry in `state/BrowserState.java` pointing at its `index.html`.
3. Make some code path in `net/` call `StateManager.transitionTo(...)`.

## Future UI states to add
- `reader/`   — reader/distraction-free mode
- `tabs/`     — tab switcher / grid
- `history/`  — browsing history & bookmarks
- `pdf/`      — PDF viewer chrome (backed by the C `pdf` feature)
- `player/`   — media player chrome (backed by the C `media` feature)
