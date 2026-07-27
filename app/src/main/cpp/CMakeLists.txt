cmake_minimum_required(VERSION 3.22.1)
project(aurora_core C)

set(CMAKE_C_STANDARD 11)

option(BUILD_AURORA_CORE "Build the native Aurora C backend" OFF)

if(BUILD_AURORA_CORE)

    # SQLite
    set(SQLITE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/sqlite)

    # libcurl
    set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
    set(BUILD_CURL_EXE OFF CACHE BOOL "" FORCE)
    set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
    set(HTTP_ONLY ON CACHE BOOL "" FORCE)
    set(CURL_USE_OPENSSL OFF CACHE BOOL "" FORCE)
    set(CURL_USE_LIBPSL OFF CACHE BOOL "" FORCE)
    set(CURL_DISABLE_INSTALL ON CACHE BOOL "" FORCE)

    add_subdirectory(third_party/curl)
    add_subdirectory(third_party/lexbor)

    add_library(aurora_core SHARED
            jni_bridge.c
            core.c
            html_parser.c
            storage.c
            network.c
            ${SQLITE_DIR}/sqlite3.c
    )

    target_include_directories(aurora_core PRIVATE
            ${CMAKE_CURRENT_SOURCE_DIR}/third_party/curl/include/curl
            ${CMAKE_CURRENT_SOURCE_DIR}/include
            ${CMAKE_CURRENT_SOURCE_DIR}/third_party/lexbor/source
            ${SQLITE_DIR}
    )

    find_library(log-lib log)

    target_link_libraries(aurora_core
            ${log-lib}
            libcurl
            lexbor
    )

    target_compile_definitions(aurora_core PRIVATE
            SQLITE_THREADSAFE=1
            SQLITE_DEFAULT_MEMSTATUS=0
            SQLITE_OMIT_DEPRECATED
    )

endif()