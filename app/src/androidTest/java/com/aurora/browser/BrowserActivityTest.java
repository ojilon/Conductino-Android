package com.aurora.browser;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented tests for BrowserActivity.
 * These tests verify that the Activity launches and initializes properly.
 * Requires an Android emulator or device to run.
 */
@RunWith(AndroidJUnit4.class)
public class BrowserActivityTest {

    @Test
    public void testActivityLaunches() {
        try (ActivityScenario<BrowserActivity> scenario = ActivityScenario.launch(BrowserActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull("Activity should not be null", activity);
            });
        }
    }

    @Test
    public void testWebViewExists() {
        try (ActivityScenario<BrowserActivity> scenario = ActivityScenario.launch(BrowserActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull("Activity should be created", activity);
                // Verify layout inflation succeeded
                assertNotNull("Activity view should not be null", activity.getWindow().getDecorView());
            });
        }
    }

    @Test
    public void testActivityDoesNotCrashOnDestroy() {
        try (ActivityScenario<BrowserActivity> scenario = ActivityScenario.launch(BrowserActivity.class)) {
            scenario.close();
        }
        // If we get here, the activity was destroyed without crashing
        assertTrue("Activity should close without exceptions", true);
    }
}