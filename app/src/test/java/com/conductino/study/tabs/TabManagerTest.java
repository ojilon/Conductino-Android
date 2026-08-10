package com.conductino.study.tabs;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TabManager is a process singleton; tests use the live instance carefully
 * (create/close within the test and assert relative counts).
 */
public class TabManagerTest {

    private TabManager tabs;

    @Before
    public void setUp() {
        tabs = TabManager.get();
        // Ensure at least one tab exists
        assertNotNull(tabs.active());
    }

    @Test
    public void createIncreasesCountAndBecomesActive() {
        int before = tabs.count();
        TabSession s = tabs.create();
        assertTrue(s.id > 0);
        assertEquals(before + 1, tabs.count());
        assertEquals(s.id, tabs.active().id);
    }

    @Test
    public void switchToChangesActive() {
        TabSession a = tabs.create();
        TabSession b = tabs.create();
        assertEquals(b.id, tabs.active().id);
        assertTrue(tabs.switchTo(a.id));
        assertEquals(a.id, tabs.active().id);
    }

    @Test
    public void closeActiveSelectsAnother() {
        TabSession a = tabs.create();
        TabSession b = tabs.create();
        tabs.switchTo(b.id);
        tabs.close(b.id);
        assertTrue(tabs.count() >= 1);
        assertNotEquals(b.id, tabs.active().id);
        // a should still be listable or another tab exists
        assertNotNull(tabs.active());
        // keep a around is fine; closing a is optional
        tabs.close(a.id);
    }

    @Test
    public void recordNavigationSetsUrlAndHistory() {
        TabSession s = tabs.create();
        tabs.recordNavigation("https://example.com", "Example");
        assertEquals("https://example.com", tabs.active().currentUrl);
        assertEquals("Example", tabs.active().title);
        assertFalse(tabs.active().historyView().isEmpty());
    }

    @Test
    public void switchToUnknownReturnsFalse() {
        assertFalse(tabs.switchTo(-99999L));
    }
}
