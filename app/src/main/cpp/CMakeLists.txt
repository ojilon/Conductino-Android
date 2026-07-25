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

#libcurl configuration
set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
set(BUILD_CURL_EXE OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE) # Build libcurl as a static library
set(HTTP_ONLY ON CACHE BOOL "" FORCE)          # Disable FTP, LDAP, etc., for now to save space

# Include the curl source tree
add_subdirectory(third_party/curl)
add_subdirectory(third_party/lexbor)

#Aurora core native library
add_library(aurora_core SHARED
        jni_bridge.c        # JNI entry points, maps to NativeCore.java
        core.c              # boot/shutdown, app dir, config
        html_parser.c       # fast search-result extraction
        storage.c           # SQLite-backed history/bookmarks
        network.c
        ${SQLITE_DIR}/sqlite3.c
)

#tell compiler where to find header files and libcurl's headers
target_include_directories(aurora_core PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/third_party/curl/include/curl
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${CMAKE_INCLUDE_CURRENT_DIR}/third_party/lexbor/source
        ${SQLITE_DIR}
)

# Android system libs.
find_library(log-lib log)

#link library against Android logs and the static libcurl library
target_link_libraries(aurora_core 
        ${log-lib}
        libcurl
        lexbor
        )

# SQLite build tuning for mobile.
target_compile_definitions(aurora_core PRIVATE
        SQLITE_THREADSAFE=1
        SQLITE_DEFAULT_MEMSTATUS=0
        SQLITE_OMIT_DEPRECATED
)
