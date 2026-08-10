#ifndef AURORA_TABS_H
#define AURORA_TABS_H

#include <stdint.h>

/* Optional C mirror of Java TabSession — implement when you move persistence
 * or multi-session logic into the native core. Java TabManager is the
 * working path today; this header documents the intended shape. */

typedef struct AuroraTabSession {
    uint64_t id;
    char    *current_url;   /* heap; free with free() */
    char    *title;
    /* future: history ring, scroll_y, last_visit */
} AuroraTabSession;

/* Namespace-style API (no classes). All return 0 on success unless noted. */
AuroraTabSession *aurora_tab_create(void);
void              aurora_tab_close(uint64_t id);
AuroraTabSession *aurora_tab_active(void);
int               aurora_tab_switch(uint64_t id);
uint32_t          aurora_tab_count(void);

#endif /* AURORA_TABS_H */
