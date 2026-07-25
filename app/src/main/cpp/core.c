#include <android/log.h>
#include <cstddef>
#include <string.h>
#include "include/aurora_core.h"
#include <stdlib.h>
#include <stdio.h>

#define TAG "Aurora/native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

//declare the external network function
int aurora_fetch_to_file(const char *url, const char *filepath);

/* storage.c owns the sqlite handle; declared here for boot/shutdown. */
extern int  aurora_storage_open(const char *app_dir);
extern void aurora_storage_close(void);
extern int aurora_cache_check(const char *url, char *out_path, size_t max_len);
extern void aurora_cache_save(const char *url, const char *localpath);

//store the app directory globally so as to write temporary files later
char g_app_dir[1024] = {0};

int aurora_boot(const char *app_dir) {
    LOGI("aurora_boot dir=%s", app_dir ? app_dir : "(null)");
    if (app_dir){
        strncpy(g_app_dir, app_dir, sizeof(g_app_dir) -1 );
    }
    return aurora_storage_open(app_dir);
}

void aurora_shutdown(void) {
    aurora_storage_close();
}

//Basic url detection
static int is_url(const char *text) {
    return  (strstr(text, "://") != NULL || strstr(text, "www.") == text || strstr(text, "localhost") != NULL);
}

char* aurora_process_request(const char *input) {
    char target_url[2048];
    char file_path[1024];
    int from_cache = 0;
    int success = 0;

    //url classification
    if (is_url(input)) {
        if (strstr(input, "://") == NULL) {
            snprintf(target_url, sizeof(target_url), "https://%s", input);
        }else {
          snprintf(target_url, sizeof(target_url), "%s", input);
        }
    }else {
        /*Fallback to DuckDuckGo search (html version for easier parsing)
        NOTE: 'input NEEDS PROPER URL ENCODING HERE, READY IT FOR PRODUCTION LEVEL
        */
        snprintf(target_url, sizeof(target_url), "https://html.duckduckgo.com/html/?q=%s", input);
    }


    LOGI("Processing request: %s", input);

    //check cache first
    if (aurora_cache_check(target_url, file_path, sizeof(file_path))) {
        //cache hit!, can skip the network completely
    }

    //return JSON payload to Java
    //for now: returning a simple payload indicating the file is ready
    char buffer[2048];
    if (success) {
        snprintf(buffer, sizeof(buffer), "{\"url\":\"%s\",\"html\":\"<h1>Download Complete</h1><p>File saved to: %s</p>\"}", 
            target_url, temp_file_path);
    }else {
        snprintf(buffer, sizeof(buffer), 
            "{\"url\":\"%s\",\"error\":\"Network request failed.\"}", 
            target_url);   
    }

    char *out = (char *)malloc(strlen(buffer) + 1);
    if(out) strcpy(out, buffer);
    return out;
}

const char *aurora_version_info(void) {
    return "aurora-core 0.1.0 (C11, sqlite3, libcurl)";
}