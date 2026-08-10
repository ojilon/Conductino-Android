# Testing

## Unit tests (`app/src/test`)

Run:

```bash
./gradlew :app:testDebugUnitTest
```

| Test class | Covers |
|------------|--------|
| `ConductinoApplicationTest` | settings.json asset present |
| `tabs/TabManagerTest` | create, switch, close, recordNavigation |
| `library/HistoryStoreTest` | add, skip local UI, collapse duplicates, JSON |
| `library/BookmarkStoreTest` | unique add, remove |
| `settings/SettingsManagerTest` | engine URL, list engines, theme id |

Uses Robolectric + `TestConductinoApplication` where Context/assets are needed.

### Adding a unit test

1. Prefer pure logic (stores, managers) over Activity.
2. Annotate with `@RunWith(RobolectricTestRunner.class)` and `@Config(application = TestConductinoApplication.class)` if you need Context.
3. Keep tests short; no network.

## Instrumented tests (`app/src/androidTest`)

```bash
./gradlew :app:connectedDebugAndroidTest
```

| Test | Covers |
|------|--------|
| `BrowserActivityTest` | launch, decor view, destroy |

Requires emulator/device.

## Notes

- `TabManager` is a singleton — tests assert relative behaviour, not a pristine empty map.
- Native C (`BUILD_AURORA_CORE`) is off by default; unit tests do not load `.so`.
