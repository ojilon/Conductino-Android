package com.conductino.study.library;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import com.conductino.study.TestConductinoApplication;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestConductinoApplication.class)
public class BookmarkStoreTest {

    private BookmarkStore store;

    @Before
    public void setUp() {
        store = BookmarkStore.get();
        store.init(RuntimeEnvironment.getApplication());
        // remove all by re-adding pattern: remove each id
        for (BookmarkStore.Entry e : store.list()) {
            store.remove(e.id);
        }
    }

    @Test
    public void addUnique() {
        assertTrue(store.add("https://bm.example", "BM"));
        assertFalse(store.add("https://bm.example", "Again"));
        assertEquals(1, store.list().size());
    }

    @Test
    public void removeWorks() {
        store.add("https://rm.example", "RM");
        long id = store.list().get(0).id;
        assertTrue(store.remove(id));
        assertEquals(0, store.list().size());
    }
}
