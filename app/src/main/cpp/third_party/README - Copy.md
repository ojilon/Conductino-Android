# `cpp/third_party/` — native libraries YOU drop in

Gradle+NDK compile these; nothing is vendored in the repo. For each lib, place
its sources/prebuilts in the named subfolder and wire it into `CMakeLists.txt`.

| Folder      | Library            | Why (kept light + robust)                      | Get it |
|-------------|--------------------|------------------------------------------------|--------|
| `sqlite/`   | **SQLite** amalgamation | history/bookmarks/cache — 1 file, battle-tested | sqlite.org/download.html → `sqlite3.c`,`sqlite3.h` |
| `mbedtls/`  | **mbedTLS**        | TLS if the C net layer does its own fetching   | github.com/Mbed-TLS/mbedtls |
| `zlib/`     | **zlib**           | gzip/deflate for HTTP + storage compression    | zlib.net |
| `brotli/`   | **brotli**         | modern content-encoding decode                 | github.com/google/brotli |

> Only `sqlite/` is referenced by the current CMake build. Uncomment/add the
> others in `CMakeLists.txt` as features come online.
