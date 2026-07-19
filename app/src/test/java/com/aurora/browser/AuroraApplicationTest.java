package com.aurora.browser;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import java.io.InputStream;

@RunWith(RobolectricTestRunner.class)
public class AuroraApplicationTest {

    @Test
    public void testSettingsJsonExistsAndIsReadable() throws Exception {
        // Access asset folder files directly in unit tests using Robolectric
        InputStream is = RuntimeEnvironment.getApplication()
                .getAssets()
                .open("config/settings.json");
        
        assertNotNull("settings.json asset should not be null", is);
        assertTrue("File should contain configuration data", is.available() > 0);
        is.close();
    }
}
