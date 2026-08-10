cmake_minimum_required(VERSION 3.22.1)
project(aurora_core C)

# C11 for existing sources. CompilerFlags.cmake is ready when we add C++23 modules.
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# Optional: pull in modern C++ flags when CXX sources are added later
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")
# include(CompilerFlags)  # enable when first .cpp lands

option(BUILD_AURORA_CORE "Build the native Aurora backend" OFF)

if(BUILD_AURORA_CORE)

    set(SQLITE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/sqlite)

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
            src/jni_bridge.c
            src/core.c
            src/html_parser.c
            src/storage.c
            src/network.c
            ${SQLITE_DIR}/sqlite3.c
    )

    target_include_directories(aurora_core PRIVATE
            ${CMAKE_CURRENT_SOURCE_DIR}/include
            ${CMAKE_CURRENT_SOURCE_DIR}/third_party/curl/include
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
