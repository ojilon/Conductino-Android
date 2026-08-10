# Themes

## Sources of truth

| Surface | File |
|---------|------|
| HTML/CSS UIs | `assets/ui/shared/theme.css` (`:root` variables) |
| Native chrome | `res/values/colors.xml` + `themes.xml` |

Keep the hex values **in sync** when you change a brand color.

## Current palette (aurora-dark)

| Token | Hex |
|-------|-----|
| bg | `#0B1020` |
| bg-elev | `#141A30` |
| fg | `#E8ECFB` |
| muted | `#8891B4` |
| accent | `#5EE7C4` |
| accent2 | `#7C7BFF` |
| border | `#26305A` |

## Switching theme later

1. Add `theme.css` variants or data-theme attributes on `<html>`.
2. Persist choice in SharedPreferences (same pattern as search engine).
3. Optionally recreate Activity or update status bar colors from Java.
4. Settings UI can list "Aurora Dark" / future light theme.

Native top bar still uses hard-coded colors in XML layout; migrate to `@color/` references when you polish chrome.
