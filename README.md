# Conductino / Aurora — Android browser foundation

A minimal, state-driven Android browser skeleton used as a foundation for features and experiments. The app runs a single WebView that loads small HTML/CSS/JS UIs from assets for each application state, while heavier logic (search parsing, indexing, extraction) lives in the native backend (C/C++ via JNI).

Quick links
- Foundation notes: [FOUNDATION.md](https://github.com/ojilon/Conductino-Android/blob/main/FOUNDATION.md)
- Native backend docs: [backend/README.md](https://github.com/ojilon/Conductino-Android/blob/main/backend/README.md)
- Testing guide: [docs/TESTING.md](https://github.com/ojilon/Conductino-Android/blob/main/docs/TESTING.md)
- Themes & UI tokens: [docs/THEMES.md](https://github.com/ojilon/Conductino-Android/blob/main/docs/THEMES.md)

What changed (short)
- Repository reorganized with a clear `backend/` native core and `app/` Android module.
- Per-state UIs live under `app/src/main/assets/ui/` and are small, independent frontends.
- Native core is built with CMake (`backend/CMakeLists.txt`); third-party native libs are kept under `backend/third_party/`.
- Tests and developer guidance are in `docs/` (see testing and themes docs above).

Why this layout
- Keep the Java/Android layer small: responsible for app lifecycle, WebView host, and chrome.
- Push parsing/indexing/extraction to a native core when performance or existing C libs are useful.
- Keep UIs simple: small HTML/CSS/JS shipped in assets so states can be iterated independently.

Prerequisites (developer machine)
- Android SDK command-line tools (sdkmanager, aapt2, d8)
- Android NDK r26+ (native core with CMake)
- Gradle 8.x (wrapper provided: `./gradlew`)
- Native third-party C libraries as needed under `backend/third_party/` (or `app/src/main/cpp/third_party/` depending on your local workflow)

Build (quick)
- Assemble debug APK:
  ./gradlew assembleDebug

- Run unit tests (JVM/Robolectric):
  ./gradlew :app:testDebugUnitTest

- Run instrumented tests (connected/emulator):
  ./gradlew :app:connectedDebugAndroidTest

If you need to build the native core, follow instructions in:
- backend/README.md — details about the native build and CMake integration.

High-level data flow (short)
JS omnibox (assets/ui/*) → BrowserBridge (Java) → NavigationController (Java)
  - If URL: Fetcher.fetchDocument → ResponseClassifier → StateManager.transitionTo(DOCUMENT)
    (downloads sub-resources as the page paints)
  - If query: SearchEngineClient → NativeCore.parseSearchResults (C/C++) → StateManager.transitionTo(RESULTS)
StateManager → WebViewHost.loadUi(assets/ui/<state>/index.html)
           → WebViewHost.emit('payload', json) → window.Aurora.on('payload')

Project layout (top-level)
```
app/                  Android module: Java code, resources, assets, NDK/CMake wiring
  src/main/java/...   Java packages (app runtime, WebView host, controllers)
  src/main/res/       Android XML (themes, colors, strings, layouts)
  src/main/assets/    UI assets: config, per-state UIs (assets/ui/), search engines
  src/main/cpp/       (optional) local native glue for the Android module
backend/              Native core (C/C++), JNI glue, CMake tooling
  CMakeLists.txt
  README.md            Native design and build instructions
  include/             public headers
  src/                 core C/C++ implementation
  third_party/         external C libs (SQLite amalgamation, etc.)
docs/                 supporting docs (testing, themes, design notes)
FOUNDATION.md         high-level roadmap and implemented areas
gradle.properties
settings.gradle
build.gradle
```

Notable files & locations
- `app/src/main/assets/ui/` — per-state HTML/CSS/JS UIs
- `app/src/main/assets/config/` — JSON config and search engine definitions
- `app/src/main/res/values/colors.xml` & `themes.xml` — native chrome tokens
- `backend/CMakeLists.txt` & `backend/README.md` — native build & integration
- Tests: see `docs/TESTING.md` and unit tests under `app/src/test`

Testing notes
- Unit tests use Robolectric when Context/assets are required.
- Native core is off by default for unit tests (see docs/TESTING.md).
- Commands and examples are in [docs/TESTING.md](https://github.com/ojilon/Conductino-Android/blob/main/docs/TESTING.md).

Contributing & developer guidance
- Small, focused PRs are easiest to review; if a feature touches native and java, split logical pieces where possible.
- Add a README to any new top-level or feature directory describing purpose and recommended libraries (this repo follows a "document-as-you-go" convention).
- See FOUNDATION.md for roadmap items and implemented areas: [FOUNDATION.md](https://github.com/ojilon/Conductino-Android/blob/main/FOUNDATION.md)

Where to start
- If you want to work on the native core: read `backend/README.md`.
- If you’re iterating UI states or themes: see `app/src/main/assets/ui/` and `docs/THEMES.md`.
- For testing guidelines and examples: `docs/TESTING.md`.

License
- If you expect a license file, add a top-level `LICENSE` with the chosen terms.

If you want, I can:
- Draft a short CONTRIBUTING.md or a developer quickstart with exact commands for setting up SDK/NDK and running the native build.
- Open a PR with this README update applied to the repository.
