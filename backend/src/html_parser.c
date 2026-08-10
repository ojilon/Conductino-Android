/* Minimal search-result / HTML rewrite foundation.
 * Full production parsing lives behind features/text or features/pdf later. */
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>
#include "aurora_core.h"

#define TAG "Aurora/parser"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

/* Stub rewrite: identity for now so the build does not require lexbor
 * until third_party is populated and BUILD_AURORA_CORE is ON with full deps.
 * When lexbor is present, restore the previous rewrite_tags implementation. */
int aurora_rewrite_html_file(const char *filepath) {
    LOGI("rewrite stub for %s (lexbor path deferred until third_party ready)", filepath);
    (void)filepath;
    return 1;
}

char *aurora_parse_search_results(const char *html, const char *selector, int limit) {
    (void)html;
    (void)selector;
    (void)limit;
    const char *empty = "{\"items\":[]}";
    char *out = (char *)malloc(strlen(empty) + 1);
    if (out) strcpy(out, empty);
    return out;
}
