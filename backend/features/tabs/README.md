# features/tabs

Native tab persistence / session helpers.

- Header sketch: `backend/include/tabs.h`
- Working implementation today: Java `com.conductino.study.tabs.TabManager`

When you implement C:

1. Add `backend/src/tabs.c` with the functions from `tabs.h`.
2. Store sessions in SQLite (reuse storage.c patterns).
3. Expose only create/close/list/active via JNI in `jni_bridge.c`.
4. Keep WebView ownership in Java.
