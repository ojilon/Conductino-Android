package com.aurora.browser;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

/**
 * Unit tests for AuroraApplication startup.
 * These tests verify that the app initializes without crashing.
 */
@RunWith(RobolectricTestRunner.class)
public class AuroraApplicationTest {

    @Test
    public void testApplicationCreates() {
        // Get the application context
        Context context = ApplicationProvider.getApplicationContext();
        assertNotNull("Application context should not be null", context);
    }

    @Test
    public void testApplicationIsInstance() {
        Context context = ApplicationProvider.getApplicationContext();
        assertTrue("Context should be an Application", context instanceof AuroraApplication);
    }

    @Test
    public void testApplicationNotNull() {
        AuroraApplication app = AuroraApplication.get();
        assertNotNull("AuroraApplication.get() should return singleton instance", app);
    }

    @Test
    public void testApplicationSingletonBehavior() {
        AuroraApplication app1 = AuroraApplication.get();
        AuroraApplication app2 = AuroraApplication.get();
        assertSame("AuroraApplication should be a singleton", app1, app2);
    }
}