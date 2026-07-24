#include <android/log.h>
#include <string.h>
#include "aurora_core.h"
#include <stdlib.h>
#include <stdio.h>

#define TAG "Aurora/native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

/* storage.c owns the sqlite handle; declared here for boot/shutdown. */
int  aurora_storage_open(const char *app_dir);
void aurora_storage_close(void);

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

char* aurora_process_request(const char *input) {
    LOGI("Processing request: %s", input);

    //prepare the temporary file path here
    char temp_file_path[1024];
    snprintf(temp_file_path, sizeof(temp_file_path), "%s/temp_reponse.html", g_app_dir);

    LOGI("Will write libcurl response to: %s", temp_file_path);

    /*Todo:
    1. Define if 'input' is a url or search query.
    2.Open the temp_file_path using fopen(temp_file_path, "wb").
    3.Execute libcurl, pointing CURLOPT_WRITEDATA to our file descriptor.
    */


    //for now: returning a dummy json payload to test the jni bridge
    const char *dummy_json = "{\"url\":\"https://c-backend-active.local\",\"html\"<h1>C Backend Active</h1><p>Ready for libcurl.</p>\"}";
    char *out = (char *)malloc(strlen(dummy_json) + 1);
    if (out) strcpy(out, dummy_json);

    return out;
}

const char *aurora_version_info(void) {
    return "aurora-core 0.1.0 (C11, sqlite3)";
}
