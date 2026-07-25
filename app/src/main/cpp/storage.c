/* SQLite-backed persistence for history/bookmarks/cache.
 * Requires third_party/sqlite/sqlite3.{c,h} (the amalgamation you provide). */
#include <android/log.h>
#include <cstddef>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "third_party/sqlite/sqlite3.h"
#include "aurora_core.h"

#define TAG "Aurora/storage"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

//assuming you have a global or static sqlite3 *db variabel from your history/bookmarks setup
extern sqlite3 *db;

//update initialization function to include the cache table
void aurora_init_schema() {
    const char *sql = "CREATE TABLE IF NOT EXISTS page_cache(url TEXT PRIMARY KEY, local_path TEXT, timestamp INTEGER);";
    sqlite3_exec(db, sql, 0, 0, 0);
}

//function to check if a valid cache exists (e.g less than 25 hours old).
int aurora_cache_check(const char *url, char *out_path, size_t max_len) {
    const char *sql = "SELECT local_path, timestamp FROM page_cache WHERE url = ?;";
    sqlite3_stmt *stmt;
    int hit = 0;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, 0) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, url, -1, SQLITE_STATIC);

        if (sqlite3_step(stmt) == SQLITE_ROW) {
            time_t now = time(NULL);
            time_t saved_time = sqlite3_column_int64(stmt, 1);

            //check if cache is fresh (e.g., 86400 seconds = 24 hours)
            if (now - saved_time < 86400) {
                const char *path =  (const char *)sqlite3_column_text(stmt, 0);
                strncpy(out_path, path, max_len - 1);
                hit = 1; //cache hit
                LOGI("Cache HIT for %s", url);
            }else {
                LOGI("Cache EXPIRED for %s", url);
            }
        }
        sqlite3_finalize(stmt);
    }
    return  hit;
}

//function to save a successful downlaod to the cache
void aurora_cache_save(const char *url, const char *local_path) {
    const char *sql = "INSERT OR REPLACE INTO page_cache(url, local_path, timestamp) VALUES(?, ?, ?);";
    sqlite3_stmt *stmt;

    if (sqlite3_prepare_v2(db, sql, -1, &stmt, 0) == SQLITE_OK) {
        sqlite3_bind_text(stmt, 1, url, -1, SQLITE_STATIC);
        sqlite3_bind_text(stmt, 2, local_path, -1, SQLITE_STATIC);
        sqlite3_bind_int64(stmt, 3, (sqlite_int64)time(NULL));

        sqlite3_step(stmt);
        sqlite3_finalize(stmt);
        LOGI("Saved %s to cache", url);
    }
}

static sqlite3 *g_db = NULL;

int aurora_storage_open(const char *app_dir) {
    char path[1024];
    snprintf(path, sizeof(path), "%s/aurora.db", app_dir ? app_dir : ".");
    if (sqlite3_open(path, &g_db) != SQLITE_OK) {
        LOGE("open failed: %s", sqlite3_errmsg(g_db));
        return 1;
    }
    const char *schema =
        "CREATE TABLE IF NOT EXISTS history("
        " id INTEGER PRIMARY KEY, url TEXT, title TEXT, ts INTEGER);"
        "CREATE TABLE IF NOT EXISTS bookmarks("
        " id INTEGER PRIMARY KEY, url TEXT UNIQUE, title TEXT);";
    char *err = NULL;
    if (sqlite3_exec(g_db, schema, NULL, NULL, &err) != SQLITE_OK) {
        LOGE("schema failed: %s", err ? err : "?");
        sqlite3_free(err);
        return 2;
    }
    return 0;
}

void aurora_storage_close(void) {
    if (g_db) { sqlite3_close(g_db); g_db = NULL; }
}

int aurora_add_history(const char *url, const char *title, long long ts) {
    if (!g_db) return 1;
    sqlite3_stmt *st;
    const char *sql = "INSERT INTO history(url,title,ts) VALUES(?,?,?);";
    if (sqlite3_prepare_v2(g_db, sql, -1, &st, NULL) != SQLITE_OK) return 2;
    sqlite3_bind_text(st, 1, url, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(st, 2, title, -1, SQLITE_TRANSIENT);
    sqlite3_bind_int64(st, 3, ts);
    int rc = sqlite3_step(st);
    sqlite3_finalize(st);
    return rc == SQLITE_DONE ? 0 : 3;
}

char *aurora_recent_history(int limit) {
    /* Foundation stub: real impl builds a JSON array from a SELECT. */
    (void) limit;
    const char *empty = "[]";
    char *out = (char *) malloc(strlen(empty) + 1);
    if (out) strcpy(out, empty);
    return out;
}
