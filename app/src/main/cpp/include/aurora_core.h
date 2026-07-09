#ifndef AURORA_CORE_H
#define AURORA_CORE_H

/* Public C API of the Aurora backend. The JNI layer (jni_bridge.c) is the
 * only translation unit that talks to the JVM; everything else is pure C so
 * the core can be reused / unit-tested off-device. */

int  aurora_boot(const char *app_dir);   /* open DB, init state. 0 = ok */
void aurora_shutdown(void);

/* Parse engine result HTML -> heap JSON array string. Caller frees.
 * 'limit' caps the number of results (browser default 300). */
char *aurora_parse_search_results(const char *html,
                                  const char *selector,
                                  int limit);

/* History (SQLite). */
int   aurora_add_history(const char *url, const char *title, long long ts);
char *aurora_recent_history(int limit);   /* JSON array; caller frees */

const char *aurora_version_info(void);

#endif /* AURORA_CORE_H */
