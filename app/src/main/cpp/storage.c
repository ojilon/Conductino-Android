/* SQLite-backed persistence for history/bookmarks/cache.
 * Requires third_party/sqlite/sqlite3.{c,h} (the amalgamation you provide). */
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "sqlite3.h"
#include "aurora_core.h"

#define TAG "Aurora/storage"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

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
