#include <android/log.h>
#include <stddef.h>
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
extern int aurora_rewrite_html_file(const char *filepath);

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
    /*
    This logic is checking cache for every url, 
    should be optimised to check cache for only the websites
    and a more complex validation of cache for the 'plain text' searches
    */
    if (aurora_cache_check(target_url, file_path, sizeof(file_path))) {
        from_cache = 1;
        success = 1;
    }else {
        snprintf(file_path, sizeof(file_path), "%s/cached_page.html", g_app_dir);

        //Execute the fetch via libcurl
        success = aurora_fetch_to_file(target_url, file_path);

        //If successful, process the html and save it to the sqlite DB
        if (success) {
            //rewrite html tags for local routing
            aurora_rewrite_html_file(file_path);

            aurora_cache_save(target_url, file_path);
        }
    }

    //prepare the temporary file path here
    char temp_file_path[1024];
    snprintf(temp_file_path, sizeof(temp_file_path), "%s/temp_reponse.html", g_app_dir);

    //return JSON payload to Java
    //for now: returning a simple payload indicating the file is ready
    char buffer[8192]; //ensure buffer is large enough for basic html
    if (success) {
        //Read modified html from disk
        FILE *fp = fopen(file_path, "rb");
        if (fp) {
            fseek(fp, 0, SEEK_END);
            long fsize = ftell(fp);
            fseek(fp, 0, SEEK_SET);

            char *html_content = (char *)malloc(fsize + 1);
            fread(html_content,1, fsize, fp);
            html_content[fsize] = 0;
            fclose(fp);

            /*
            NOTE: In production, escape double quotes in html_content
            before embedding it in json string.
            */
            snprintf(buffer, sizeof(buffer), 
                "{\"url\":\"%s\",\"html\":\"%s\"}", target_url, html_content);
                
            free(html_content);
        }
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

char* aurora_resolve_local_path(const char* url) {
    /*
    For now, since we hardcoded "aurora-local://resource/placeholder",
    we will return a dummy path or a placeholder image path.
    In the fully built version, this will query an SQLite table or hash the URL. 
    */

    if (strstr(url, "aurora-local://") == url) {
        char buffer[1024];

        //point this to where you save the downlaod assets
        snprintf(buffer, sizeof(buffer), "%s/assets/placeholder.png", g_app_dir);

        FILE *file = fopen(buffer, "r");
        if (!file) {
            /*
            If missing, block and fetch it using existing network logic
            In a complete implementation, one would extract the REAL url that lexbor parsed,
            but for now simulate fetching the asset.
            */

            const char *real_asset_url = "https://example.com/actual-image.png";
            aurora_fetch_to_file(real_asset_url,buffer);
        }else {
            fclose(file);
        }

        char *out = (char *)malloc(strlen(buffer) + 1);
        if (out) strcpy(out, buffer);
        return out;
    }
    return NULL;
}