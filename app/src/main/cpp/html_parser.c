/* Minimal, allocation-light search-result extractor.
 *
 * This is the FOUNDATION: it returns an empty JSON array. A production version
 * would run a fast forward-only scan (or embed a lib like lexbor/gumbo — see
 * cpp/features/html/README.md) to pull <a href>/title/snippet triples matching
 * the engine's selector, stopping at 'limit'. Doing this in C keeps the big
 * string work off the JVM heap. */
#include <stdlib.h>
#include <string.h>
#include "include/aurora_core.h"
#include <stdio.h>
#include <android/log.h>
#include "third_party/lexbor/source/lexbor/core/types.h"
#include "third_party/lexbor/source/lexbor/html/parser.h"
#include "third_party/lexbor/source/lexbor/dom/interfaces/element.h"
#include "third_party/lexbor/source/lexbor/html/serialize.h"
#include "third_party/lexbor/source/lexbor/html/parser.h"

#define TAG "Aurora/parser"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

//callback for lexbor serialization to write back to file
static lxb_status_t serialize_cb(const lxb_char_t *data, size_t len, void *ctx) {
    FILE *fp = (FILE *)ctx;
    fwrite(data, 1, len, fp);
    return LXB_STATUS_OK;
}

//Helper to rewrite attributes in a collection of nodes
static void rewrite_tags(lxb_dom_document_t *dom, const char *tag_name, const char *attr_name) {
    lxb_dom_collection_t *collection = lxb_dom_collection_make(dom, 128);
    if (!collection) return;

    // 1. Safely cast the generic document to an HTML document
    lxb_html_document_t *html_doc = (lxb_html_document_t *)dom->node.owner_document;

    // 2. Get the body element
    lxb_dom_element_t *body_element = lxb_dom_interface_element(html_doc->body);

    // 3. Call the function with the correct argument order (collection goes second!)
    lxb_status_t status = lxb_dom_elements_by_tag_name(
        body_element, 
        collection, 
        (const lxb_char_t *)tag_name, 
        strlen(tag_name)
    );

    if (status == LXB_STATUS_OK) {
        for (size_t i = 0; i < lxb_dom_collection_length(collection); i++) {
            lxb_dom_element_t *element = lxb_dom_interface_element(lxb_dom_collection_node(collection, i));

            /*
            In a full implementation, you would extract the original url here,
            push it to a libcurl downlaod queue, and generate a specfic local path.
            For now, we rewrite it to a generic local placeholder path to test the routing.
            */
            const lxb_char_t*local_path = (const lxb_char_t *)"aurora-local://resource/placeholder";

            lxb_dom_element_set_attribute(element, (const lxb_char_t *)attr_name, strlen(attr_name), local_path, strlen((const char *)local_path));
        }
    }
    lxb_dom_collection_destroy(collection, true);
}

//Main function to parse and rewrite the html file
int aurora_rewrite_html_file(const char *filepath) {
    LOGI("Rewriting HTML file: %s", filepath);

    //read the file into memory
    FILE *fp = fopen(filepath, "rb");
    if (!fp) return 0;

    fseek(fp, 0, SEEK_END);
    long fsize = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    char *html_data = (char *)malloc(fsize + 1);
    fread(html_data, 1, fsize, fp);
    fclose(fp);
    html_data[fsize] = 0;

    //Initialize lexbor and Parse
    lxb_html_parser_t *parser = lxb_html_parser_create();
    lxb_status_t status = lxb_html_parser_init(parser);

    lxb_html_document_t *document = lxb_html_parse(parser, (const lxb_char_t *)html_data, fsize);
    free(html_data); //free the raw buffer early

    if (document != NULL) {
        //rewrite links
        lxb_dom_document_t *dom = &document->dom_document;
        rewrite_tags(dom, "img", "src");
        rewrite_tags(dom, "link", "href");
        rewrite_tags(dom, "script", "src");

        //serialize back to the same file
        fp = fopen(filepath, "wb");
        if(fp) {
            lxb_html_serialize_tree_cb(lxb_dom_interface_node(document), serialize_cb, fp);
            fclose(fp);
        }

        lxb_html_document_destroy(document);
    }

    lxb_html_parser_destroy(parser);
    return 1;
}

char *aurora_parse_search_results(const char *html, const char *selector, int limit) {
    (void) html; (void) selector; (void) limit;
    const char *empty = "{\"items\":[]}";
    char *out = (char *) malloc(strlen(empty) + 1);
    if (out) strcpy(out, empty);
    return out;
}
