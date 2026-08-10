# Conductino – Foundation Map

Branch: `front_end`. Chrome is stable; extend via modules + READMEs.

## Implemented (v0)

| Area | Location |
|------|----------|
| Layout | `activity_browser.xml` |
| Tabs + switcher | `tabs/TabManager`, UI `assets/ui/tabs/`, bridge `switchTab`/`closeTab` |
| Search engines | `SettingsManager`, Settings picker |
| Sidebar | state-aware options including **Tabs** |
| Downloads | `downloads/DownloadStore` |
| History / bookmarks | `library/*` |
| Find / reader | `content/PageContentHelper`, `assets/ui/document/` |
| Themes | `theme.css` (`aurora-dark` / `aurora-dim`), Settings picker, `setTheme` |
| Native core path | `backend/` |
| Tests | `docs/TESTING.md`, unit tests under `app/src/test` |

## Session complete

Further work (when you resume):

1. Persist tabs across process death  
2. Export downloads to public MediaStore  
3. Find prev/next bar  
4. Readability / AI on extracted text  
5. Apply theme to native top bar colors dynamically  

Run unit tests: `./gradlew :app:testDebugUnitTest`
