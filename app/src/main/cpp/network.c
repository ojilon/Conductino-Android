#include <stddef.h>
#include <stdio.h>
#include <string.h>
#include "third_party/curl/include/curl/curl.h"
#include "third_party/curl/include/curl/easy.h"
#include <android/log.h>

#define TAG "Aurora/network"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

//The callback that writes network chunks directly to disk
static size_t write_callback(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    FILE *fp = (FILE *)userp;
    if (fp) {
        return fwrite(contents, size, nmemb, fp);
    }
    return  0;
}

//The core fetch function
int aurora_fetch_to_file(const char *url, const char *filepath) {
    CURL *curl;
    CURLcode res;
    int success = 0;

    LOGI("Fetching %s to %s", url, filepath);

    curl =  curl_easy_init();
    if (curl) {
        FILE *fp = fopen(filepath, "wb");
        if (fp) {
            //set the url
            curl_easy_setopt(curl, CURLOPT_URL, url);

            //set the user agent to match the previous 'SettingsManager' default
            curl_easy_setopt(curl, CURLOPT_USERAGENT, "AuroraBrowser/0.1");

            //tell libcurl to follow redirects (HTTP 3xx)
            curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);

            //Write up the file writing callback
            curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
            curl_easy_setopt(curl, CURLOPT_WRITEDATA, fp);

            //Execute the request
            res = curl_easy_perform(curl);
            if (res != CURLE_OK) {
                LOGE("curl_easy_perform() failed: %s", curl_easy_strerror(res));
            }else {
            success = 1; //mark as successful
            }

            fclose(fp);
        }else {
        LOGE("Failed to open file for writing: %s", filepath);
        }

        //clean up the curl handle
        curl_easy_cleanup(curl);
    }
    return  success;
}