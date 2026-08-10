# Conductino – Foundation Map

Chrome stays stable. New features land as modules + short guides.

---

## Implemented (v0)

| § | Topic | Where |
|---|--------|--------|
| 1 | Layout | `activity_browser.xml` |
| 2 | Tabs | `tabs/TabManager`, `tabs/README.md` |
| 3 | Search engines | `SettingsManager`, `settings/README.md` |
| 4 | Sidebar | `BrowserActivity.populateSidebarOptions` |
| 5 | Downloads | `downloads/DownloadStore`, UI `assets/ui/downloads/` |
| 6 | History & bookmarks | `library/*`, UI `history/` + `bookmarks/` |
| 7 | Page content | `content/PageContentHelper`, reader `assets/ui/document/` |
| 8 | C++ layout | `backend/` |
| 9 | Themes | `theme.css`, `colors.xml`, `docs/THEMES.md` |

---

## §7 Page content — details

- **Find in page:** dialog → `PageContentHelper.findInPage` / `clearFind`
- **Reader:** extract `document.body.innerText` → `BrowserState.DOCUMENT` with JSON `{title,url,text}`
- Guide: `content/README.md` (Readability, selection, AI, C++ next)

---

## §9 Themes — details

- HTML tokens: `assets/ui/shared/theme.css`
- Native: `res/values/colors.xml`, `themes.xml`
- Guide: `docs/THEMES.md` — keep hex values in sync

---

## Extend next (your roadmap)

1. Tab switcher UI + persistence
2. Download export to public MediaStore
3. Find bar with prev/next
4. Readability.js or C++ article extract
5. Theme picker in Settings
6. AI summarise harness on extracted text

---

*Foundation pass complete on branch `front_end`.*
