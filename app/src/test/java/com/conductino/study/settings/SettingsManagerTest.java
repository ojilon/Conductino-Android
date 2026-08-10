package com.conductino.study.settings;

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
public class SettingsManagerTest {

    private SettingsManager settings;

    @Before
    public void setUp() {
        settings = SettingsManager.get();
        settings.load(RuntimeEnvironment.getApplication());
    }

    @Test
    public void defaultEngineIsKnown() {
        String id = settings.defaultEngine();
        assertNotNull(id);
        assertFalse(settings.engineQueryUrl(id).isEmpty());
    }

    @Test
    public void buildSearchUrlSubstitutesQuery() {
        settings.setDefaultEngine("duckduckgo");
        String url = settings.buildSearchUrl("hello world");
        assertTrue(url.contains("hello") || url.contains("hello+") || url.contains("hello%20"));
        assertFalse(url.contains("{query}"));
    }

    @Test
    public void listEnginesNotEmpty() {
        assertTrue(settings.listEngines().size() >= 2);
    }

    @Test
    public void setThemePersists() {
        settings.setThemeId("aurora-dark");
        assertEquals("aurora-dark", settings.themeId());
    }
}
