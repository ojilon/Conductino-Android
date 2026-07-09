# sqlite/

Drop the **SQLite amalgamation** here:

```
sqlite3.c
sqlite3.h
```

Download: https://www.sqlite.org/download.html (the "amalgamation" zip).

Already referenced by `CMakeLists.txt` and used by `storage.c` for the
history/bookmarks tables. No extra build flags needed beyond the ones set in
CMake (`SQLITE_THREADSAFE=1`, etc.).
