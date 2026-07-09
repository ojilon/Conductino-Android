/* JNI bridge — maps the native methods declared in
 * com.aurora.browser.core.NativeCore to the pure-C core functions. */
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "aurora_core.h"

#define CLS "Java_com_aurora_browser_core_NativeCore_"

static jstring c_to_j(JNIEnv *env, char *s) {
    if (!s) return (*env)->NewStringUTF(env, "");
    jstring j = (*env)->NewStringUTF(env, s);
    free(s);
    return j;
}

JNIEXPORT jint JNICALL
Java_com_aurora_browser_core_NativeCore_nativeBoot(JNIEnv *env, jobject thiz, jstring app_dir) {
    const char *dir = (*env)->GetStringUTFChars(env, app_dir, NULL);
    int rc = aurora_boot(dir);
    (*env)->ReleaseStringUTFChars(env, app_dir, dir);
    return rc;
}

JNIEXPORT jstring JNICALL
Java_com_aurora_browser_core_NativeCore_parseSearchResults(JNIEnv *env, jobject thiz,
        jstring html, jstring selector, jint limit) {
    const char *h = (*env)->GetStringUTFChars(env, html, NULL);
    const char *s = (*env)->GetStringUTFChars(env, selector, NULL);
    char *json = aurora_parse_search_results(h, s, (int) limit);
    (*env)->ReleaseStringUTFChars(env, html, h);
    (*env)->ReleaseStringUTFChars(env, selector, s);
    return c_to_j(env, json);
}

JNIEXPORT void JNICALL
Java_com_aurora_browser_core_NativeCore_addHistory(JNIEnv *env, jobject thiz,
        jstring url, jstring title, jlong ts) {
    const char *u = (*env)->GetStringUTFChars(env, url, NULL);
    const char *t = (*env)->GetStringUTFChars(env, title, NULL);
    aurora_add_history(u, t, (long long) ts);
    (*env)->ReleaseStringUTFChars(env, url, u);
    (*env)->ReleaseStringUTFChars(env, title, t);
}

JNIEXPORT jstring JNICALL
Java_com_aurora_browser_core_NativeCore_recentHistory(JNIEnv *env, jobject thiz, jint limit) {
    return c_to_j(env, aurora_recent_history((int) limit));
}

JNIEXPORT jstring JNICALL
Java_com_aurora_browser_core_NativeCore_versionInfo(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, aurora_version_info());
}
