#include <android/log.h>
#include <stddef.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "aurora_core.h"

#define TAG "Aurora/native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

int aurora_fetch_to_file(const char *url, const char *filepath);

extern int  aurora_storage_open(const char *app_dir);
extern void aurora_storage_close(void);
extern int  aurora_cache_check(const char *url, char *out_path, size_t max_len);
extern void aurora_cache_save(const char *url, const char *localpath);
extern int  aurora_rewrite_html_file(const char *filepath);

char g_app_dir[1024] = {0};

int aurora_boot(const char *app_dir) {
    LOGI("aurora_boot dir=%s", app_dir ? app_dir : "(null)");
    if (app_dir) {
        strncpy(g_app_dir, app_dir, sizeof(g_app_dir) - 1);
    }
    return aurora_storage_open(app_dir);
}

void aurora_shutdown(void) {
    aurora_storage_close();
}

static int is_url(const char *text) {
    return (strstr(text, "://") != NULL
            || strstr(text, "www.") == text
            || strstr(text, "localhost") != NULL);
}

char* aurora_process_request(const char *input) {
    char target_url[2048];
    char file_path[1024];
    int from_cache = 0;
    int success = 0;

    if (is_url(input)) {
        if (strstr(input, "://") == NULL) {
            snprintf(target_url, sizeof(target_url), "https://%s", input);
        } else {
            snprintf(target_url, sizeof(target_url), "%s", input);
        }
    } else {
        /* Fallback search — production must URL-encode input. */
        snprintf(target_url, sizeof(target_url),
                 "https://html.duckduckgo.com/html/?q=%s", input);
    }

    LOGI("Processing request: %s", input);

    if (aurora_cache_check(target_url, file_path, sizeof(file_path))) {
        from_cache = 1;
        success = 1;
        (void)from_cache;
    } else {
        snprintf(file_path, sizeof(file_path), "%s/cached_page.html", g_app_dir);
        success = aurora_fetch_to_file(target_url, file_path);
        if (success) {
            aurora_rewrite_html_file(file_path);
            aurora_cache_save(target_url, file_path);
        }
    }

    char buffer[8192];
    if (success) {
        FILE *fp = fopen(file_path, "rb");
        if (fp) {
            fseek(fp, 0, SEEK_END);
            long fsize = ftell(fp);
            fseek(fp, 0, SEEK_SET);
            char *html_content = (char *)malloc((size_t)fsize + 1);
            if (html_content) {
                fread(html_content, 1, (size_t)fsize, fp);
                html_content[fsize] = 0;
                /* Production: escape quotes before embedding in JSON. */
                snprintf(buffer, sizeof(buffer),
                         "{\"url\":\"%s\",\"html\":\"%s\"}",
                         target_url, html_content);
                free(html_content);
            } else {
                snprintf(buffer, sizeof(buffer),
                         "{\"url\":\"%s\",\"error\":\"oom\"}", target_url);
            }
            fclose(fp);
        } else {
            snprintf(buffer, sizeof(buffer),
                     "{\"url\":\"%s\",\"error\":\"open failed\"}", target_url);
        }
    } else {
        snprintf(buffer, sizeof(buffer),
                 "{\"url\":\"%s\",\"error\":\"Network request failed.\"}",
                 target_url);
    }

    char *out = (char *)malloc(strlen(buffer) + 1);
    if (out) strcpy(out, buffer);
    return out;
}

const char *aurora_version_info(void) {
    return "aurora-core 0.1.0 (C11, sqlite3, libcurl)";
}

char* aurora_resolve_local_path(const char* url) {
    if (strstr(url, "aurora-local://") == url) {
        char buffer[1024];
        snprintf(buffer, sizeof(buffer), "%s/assets/placeholder.png", g_app_dir);
        FILE *file = fopen(buffer, "r");
        if (!file) {
            const char *real_asset_url = "https://example.com/actual-image.png";
            aurora_fetch_to_file(real_asset_url, buffer);
        } else {
            fclose(file);
        }
        char *out = (char *)malloc(strlen(buffer) + 1);
        if (out) strcpy(out, buffer);
        return out;
    }
    return NULL;
}
