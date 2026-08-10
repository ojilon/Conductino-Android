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
public class HistoryStoreTest {

    private HistoryStore store;

    @Before
    public void setUp() {
        store = HistoryStore.get();
        store.init(RuntimeEnvironment.getApplication());
        store.clear();
    }

    @Test
    public void addAndList() {
        store.add("https://a.example", "A");
        store.add("https://b.example", "B");
        assertEquals(2, store.list(10).size());
        assertEquals("https://b.example", store.list(1).get(0).url);
    }

    @Test
    public void skipsLocalUiUrls() {
        store.add("https://appassets.androidplatform.net/ui/welcome/index.html", "W");
        assertEquals(0, store.list(10).size());
    }

    @Test
    public void collapsesConsecutiveDuplicates() {
        store.add("https://same.example", "One");
        store.add("https://same.example", "Two");
        assertEquals(1, store.list(10).size());
        assertEquals("Two", store.list(1).get(0).title);
    }

    @Test
    public void listAsJsonIsArray() {
        store.add("https://x.example", "X");
        String json = store.listAsJson(5);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("x.example"));
    }
}
