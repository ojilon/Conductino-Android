# settings/ — search engines & config

## Files

| Path | Role |
|------|------|
| `assets/config/search_engines.json` | Engine catalog (DuckDuckGo, Google, Brave, …) |
| `assets/config/settings.json` | App defaults (appearance, network, …) |
| `SettingsManager.java` | Load JSON, persist selected engine, `buildSearchUrl()` |
| `NavigationController` | Uses `buildSearchUrl()` for non-URL omnibox input |

## Behaviour (v0)

- On launch, `ConductinoApplication` calls `SettingsManager.load()`.
- Selected engine id is stored in SharedPreferences (`conductino_settings`).
- Omnibox / welcome search: if input is not a URL → `buildSearchUrl(query)`.
- Settings HTML lists engines; tapping one calls `Aurora.selectEngine(id)` → bridge → `NavigationController.setEngine`.

## Add a built-in engine

Edit `search_engines.json`:

```json
{
  "id": "startpage",
  "name": "Startpage",
  "queryUrl": "https://www.startpage.com/sp/search?query={query}"
}
```

`{query}` is replaced with URL-encoded text.

## Custom engine at runtime (extend later)

1. Append to an in-memory JSONArray in SettingsManager.
2. Write a mutable copy under `filesDir/config/search_engines.json`.
3. Prefer loading filesDir over assets when present.

## Settings UI payload

When opening Settings, Java can emit:

```json
{
  "engine": "duckduckgo",
  "engineName": "DuckDuckGo",
  "engines": [ {"id":"duckduckgo","name":"DuckDuckGo"}, ... ],
  "maxResults": 300
}
```

Wire that via `StateManager.transitionTo(SETTINGS, payloadJson)` when you want live data instead of static HTML.
