/* Minimal, allocation-light search-result extractor.
 *
 * This is the FOUNDATION: it returns an empty JSON array. A production version
 * would run a fast forward-only scan (or embed a lib like lexbor/gumbo — see
 * cpp/features/html/README.md) to pull <a href>/title/snippet triples matching
 * the engine's selector, stopping at 'limit'. Doing this in C keeps the big
 * string work off the JVM heap. */
#include <stdlib.h>
#include <string.h>
#include "aurora_core.h"

char *aurora_parse_search_results(const char *html, const char *selector, int limit) {
    (void) html; (void) selector; (void) limit;
    const char *empty = "{\"items\":[]}";
    char *out = (char *) malloc(strlen(empty) + 1);
    if (out) strcpy(out, empty);
    return out;
}
