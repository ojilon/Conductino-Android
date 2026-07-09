#include <android/log.h>
#include <string.h>
#include "aurora_core.h"

#define TAG "Aurora/native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

/* storage.c owns the sqlite handle; declared here for boot/shutdown. */
int  aurora_storage_open(const char *app_dir);
void aurora_storage_close(void);

int aurora_boot(const char *app_dir) {
    LOGI("aurora_boot dir=%s", app_dir ? app_dir : "(null)");
    return aurora_storage_open(app_dir);
}

void aurora_shutdown(void) {
    aurora_storage_close();
}

const char *aurora_version_info(void) {
    return "aurora-core 0.1.0 (C11, sqlite3)";
}
