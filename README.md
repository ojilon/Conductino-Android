# Aurora Browser — Android foundation

A state-driven Android browser skeleton. **Java** orchestrates a single
`WebView`; the frontend is many small **HTML/CSS/JS** UIs (one per state); the
**C** backend (via **JNI**) handles the heavy work (SQLite, HTML scanning, and
future media/pdf/text modules). Config lives in **JSON**; chrome in **XML**.

## Build tooling (assumed present — not installed by this repo)
- **Gradle 8.x** (`./gradlew assembleDebug`)
- **Android SDK** command-line tools (`sdkmanager`, `aapt2`, `d8`)
- **Android NDK r26+** (compiles the C core via CMake)
- Third-party C libs you supply under `app/src/main/cpp/third_party/`
  (start with the **SQLite** amalgamation — see that folder's README).

## Data flow (type text in the URL bar → see a result)
```
JS omnibox ─Aurora.submit()→ BrowserBridge (api/)
   → NavigationController (net/)
       ├ URL?  → Fetcher.fetchDocument → ResponseClassifier
       │           → StateManager.transitionTo(DOCUMENT)  (downloads HTML;
       │             sub-resources STREAM via ResourceRouter as it paints)
       └ query → SearchEngineClient → NativeCore.parseSearchResults (C)
                   → StateManager.transitionTo(RESULTS)   (≤ 300 items)
                   (single direct hit → navigate straight to the site)
StateManager → WebViewHost.loadUi(assets/ui/<state>/index.html)
             → WebViewHost.emit('payload', json) → window.Aurora.on('payload')
```

## Layout
```
settings.gradle.kts / build.gradle.kts / gradle.properties   Gradle build
app/build.gradle.kts                    module + NDK/CMake wiring
app/src/main/AndroidManifest.xml        permissions, launcher, browser intent
app/src/main/res/                       XML: layout, theme, colors, strings
app/src/main/assets/config/*.json       settings + search engines (+ schema)
app/src/main/assets/ui/<state>/         per-state HTML/CSS/JS  (see ui/README)
app/src/main/java/com/aurora/browser/   Java packages (see features/README)
app/src/main/cpp/                       C backend + JNI (see third_party & features READMEs)
```

Every empty folder ships a `README.md` describing the feature to add there and
which light, robust library to use.
