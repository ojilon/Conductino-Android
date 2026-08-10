/* JNI bridge — maps native methods in com.conductino.study.core.NativeCore
 * to pure-C core functions. Keep this file thin. */
#include <stddef.h>
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "aurora_core.h"

static jstring c_to_j(JNIEnv *env, char *s) {
    if (!s) return (*env)->NewStringUTF(env, "");
    jstring j = (*env)->NewStringUTF(env, s);
    free(s);
    return j;
}

/* NOTE: JNI symbol names must match the Java package + class exactly.
 * Current Java class: com.conductino.study.core.NativeCore
 * If symbols fail to resolve at runtime, align these names with that class. */

JNIEXPORT jint JNICALL
Java_com_conductino_study_core_NativeCore_nativeBoot(JNIEnv *env, jobject thiz, jstring app_dir) {
    (void)thiz;
    const char *dir = (*env)->GetStringUTFChars(env, app_dir, NULL);
    int rc = aurora_boot(dir);
    (*env)->ReleaseStringUTFChars(env, app_dir, dir);
    return rc;
}

JNIEXPORT jstring JNICALL
Java_com_conductino_study_core_NativeCore_parseSearchResults(JNIEnv *env, jobject thiz,
        jstring html, jstring selector, jint limit) {
    (void)thiz;
    const char *h = (*env)->GetStringUTFChars(env, html, NULL);
    const char *s = (*env)->GetStringUTFChars(env, selector, NULL);
    char *json = aurora_parse_search_results(h, s, (int)limit);
    (*env)->ReleaseStringUTFChars(env, html, h);
    (*env)->ReleaseStringUTFChars(env, selector, s);
    return c_to_j(env, json);
}

JNIEXPORT void JNICALL
Java_com_conductino_study_core_NativeCore_addHistory(JNIEnv *env, jobject thiz,
        jstring url, jstring title, jlong ts) {
    (void)thiz;
    const char *u = (*env)->GetStringUTFChars(env, url, NULL);
    const char *t = (*env)->GetStringUTFChars(env, title, NULL);
    aurora_add_history(u, t, (long long)ts);
    (*env)->ReleaseStringUTFChars(env, url, u);
    (*env)->ReleaseStringUTFChars(env, title, t);
}

JNIEXPORT jstring JNICALL
Java_com_conductino_study_core_NativeCore_recentHistory(JNIEnv *env, jobject thiz, jint limit) {
    (void)thiz;
    return c_to_j(env, aurora_recent_history((int)limit));
}

JNIEXPORT jstring JNICALL
Java_com_conductino_study_core_NativeCore_versionInfo(JNIEnv *env, jobject thiz) {
    (void)thiz;
    return (*env)->NewStringUTF(env, aurora_version_info());
}

JNIEXPORT jstring JNICALL
Java_com_conductino_study_core_NativeCore_processRequest(JNIEnv *env, jobject thiz, jstring input) {
    (void)thiz;
    const char *c_input = (*env)->GetStringUTFChars(env, input, NULL);
    char *json_response = aurora_process_request(c_input);
    (*env)->ReleaseStringUTFChars(env, input, c_input);
    return c_to_j(env, json_response);
}

JNIEXPORT jstring JNICALL
Java_com_conductino_study_core_NativeCore_getLocalResourcePath(JNIEnv *env, jobject thiz, jstring localUrl) {
    (void)thiz;
    const char *c_url = (*env)->GetStringUTFChars(env, localUrl, NULL);
    extern char* aurora_resolve_local_path(const char* url);
    char *real_path = aurora_resolve_local_path(c_url);
    (*env)->ReleaseStringUTFChars(env, localUrl, c_url);
    if (real_path) return c_to_j(env, real_path);
    return NULL;
}
