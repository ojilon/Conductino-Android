# ─────────────────────────────────────────────────────────────
# Aurora C backend — built by the NDK via CMake, invoked from Gradle.
# Produces libaurora_core.so, loaded by NativeCore.java.
#
# Libraries used (YOU provide the sources/prebuilts under cpp/third_party/):
#   • sqlite3   — history / bookmarks / cache DB   (amalgamation: sqlite3.c)
#   • (future)  — see cpp/features/*/README.md for lib recommendations.
# ─────────────────────────────────────────────────────────────
cmake_minimum_required(VERSION 3.22.1)
project(aurora_core C)

set(CMAKE_C_STANDARD 11)

# --- SQLite amalgamation (drop sqlite3.c + sqlite3.h in third_party/sqlite) ---
set(SQLITE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/sqlite)

add_library(aurora_core SHARED
        jni_bridge.c        # JNI entry points, maps to NativeCore.java
        core.c              # boot/shutdown, app dir, config
        html_parser.c       # fast search-result extraction
        storage.c           # SQLite-backed history/bookmarks
        ${SQLITE_DIR}/sqlite3.c
)

target_include_directories(aurora_core PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${SQLITE_DIR}
)

# Android system libs.
find_library(log-lib log)
target_link_libraries(aurora_core ${log-lib})

# SQLite build tuning for mobile.
target_compile_definitions(aurora_core PRIVATE
        SQLITE_THREADSAFE=1
        SQLITE_DEFAULT_MEMSTATUS=0
        SQLITE_OMIT_DEPRECATED
)
