package com.aurora.browser.state;

import com.aurora.browser.logging.LogManager;
import com.aurora.browser.web.WebViewHost;

/**
 * Central authority that decides WHICH ui/<state> document is rendered.
 *
 * The network layer classifies each response, then asks StateManager to
 * transition. StateManager loads that state's index.html and emits the
 * payload to it once loaded.
 */
public class StateManager {

    private static final StateManager INSTANCE = new StateManager();
    public static StateManager get() { return INSTANCE; }
    private StateManager() {}

    private WebViewHost host;
    private BrowserState current = BrowserState.WELCOME;
    private String pendingPayload;

    public void bind(WebViewHost host) {
        this.host = host;
    }

    /** Swap the UI. payloadJson is delivered to the page after it loads. */
    public void transitionTo(BrowserState state, String payloadJson) {
        LogManager.i("StateManager", "transition " + current + " -> " + state);
        this.current = state;
        this.pendingPayload = payloadJson;
        if (host != null) {
            host.loadUi(state.indexPath());
        }
    }

    /** Called by the JS bridge once a UI page signals it is ready. */
    public void onUiReady() {
        if (host != null && pendingPayload != null) {
            host.emit("payload", pendingPayload);
            pendingPayload = null;
        }
    }

    public BrowserState current() {
        return current;
    }
}
