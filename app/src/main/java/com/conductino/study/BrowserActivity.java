package com.conductino.study;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.conductino.study.logging.LogManager;
import com.conductino.study.state.BrowserState;
import com.conductino.study.state.StateManager;
import com.conductino.study.web.WebViewHost;
import com.conductino.study.net.NavigationController;

import java.util.ArrayList;
import java.util.List;

/**
 * Single Activity that hosts the WebView chrome + content area.
 * Delegates real work to specialized packages:
 *
 *   web/       -> WebView + JS bridge
 *   state/     -> which assets/ui/<state> to show
 *   net/       -> navigation / download decisions
 *   api/       -> JS <-> Java contract
 *   core/      -> JNI into C++ backend (future tabs, storage, etc.)
 *   settings/  -> settings.json + search engines
 *   logging/   -> LogManager
 *
 * Tabs, bookmarks, downloads store, content extraction, etc. are
 * intentionally left as short stubs + comments so the skeleton stays stable.
 */
public class BrowserActivity extends AppCompatActivity {

    private WebViewHost host;
    private WebView webView;
    private EditText urlBar;
    private ProgressBar progressBar;

    private DrawerLayout drawerLayout;
    private ImageButton btnHome;
    private ImageButton btnAddTab;
    private ImageButton btnMenu;
    private LinearLayout drawerOptionsContainer;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean isGranted : result.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    LogManager.i("Activity", "All permissions granted by user.");
                } else {
                    LogManager.i("Activity", "Permissions denied.");
                    Toast.makeText(this, "Storage permissions are required for downloads.", Toast.LENGTH_LONG).show();
                }
                initializeBrowserEngine();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        drawerLayout = findViewById(R.id.drawer_layout);
        webView = findViewById(R.id.web_view);
        urlBar = findViewById(R.id.url_bar);
        btnHome = findViewById(R.id.btn_home);
        btnAddTab = findViewById(R.id.btn_add_tab);
        btnMenu = findViewById(R.id.btn_menu);
        progressBar = findViewById(R.id.progress_bar);
        drawerOptionsContainer = findViewById(R.id.drawer_options_container);

        setupNativeUI();

        if (hasRequiredPermissions()) {
            initializeBrowserEngine();
        } else {
            LogManager.i("Activity", "Requesting permissions asynchronously...");
            requestPermissionLauncher.launch(getRequiredPermissionsArray());
        }
    }

    private void setupNativeUI() {
        // Omnibox: Enter / Go
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String input = urlBar.getText().toString().trim();
                LogManager.i("Activity", "Omnibox submit: " + input);
                NavigationController.get().handleInput(input);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(urlBar.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Home -> always return to the browser welcome surface
        btnHome.setOnClickListener(v -> {
            LogManager.i("Activity", "Home clicked");
            goToWelcome();
        });

        // New Tab -> foundational stub: open a fresh welcome session
        // Later this will call into C++ / TabManager to create an isolated session
        btnAddTab.setOnClickListener(v -> {
            LogManager.i("Activity", "New Tab clicked");
            // TODO: TabManager.createTab() -> new session id, history, etc.
            goToWelcome();
            Toast.makeText(this, "New tab (welcome)", Toast.LENGTH_SHORT).show();
        });

        // Menu -> populate context-sensitive options and open drawer
        btnMenu.setOnClickListener(v -> {
            LogManager.i("Activity", "Menu clicked");
            populateSidebarOptions();
            drawerLayout.openDrawer(GravityCompat.END);
        });
    }

    /** Shared path for Home and New-Tab for now. */
    private void goToWelcome() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        StateManager.get().transitionTo(BrowserState.WELCOME, null);
        if (urlBar != null) {
            urlBar.setText("");
            urlBar.clearFocus();
        }
    }

    private void populateSidebarOptions() {
        drawerOptionsContainer.removeAllViews();
        BrowserState current = StateManager.get().current();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);

        if (current == BrowserState.WELCOME) {
            // Welcome-state options (browser chrome)
            addSidebarOption("New Tab", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                goToWelcome();
            });
            addSidebarOption("History", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                // TODO: open history UI (local store / C++ later)
                Toast.makeText(this, "History (stub)", Toast.LENGTH_SHORT).show();
            });
            addSidebarOption("Downloads", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                // TODO: internal downloads store + export to public Downloads
                Toast.makeText(this, "Downloads (stub)", Toast.LENGTH_SHORT).show();
            });
            addSidebarOption("Settings", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                StateManager.get().transitionTo(BrowserState.SETTINGS, null);
            });
        } else {
            // Page / content-state options
            addSidebarOption("Refresh", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                if (webView != null) webView.reload();
            });
            addSidebarOption("Find in page", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                // Basic example of content interaction via WebView
                // Full UI can later be an HTML overlay or native dialog
                if (webView != null) {
                    webView.findAllAsync(""); // clears; real UI will call with query
                    Toast.makeText(this, "Find in page (stub – use WebView.findAllAsync)", Toast.LENGTH_SHORT).show();
                }
            });
            addSidebarOption("Bookmark", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                // TODO: BookmarkStore.add(currentUrl, title)
                Toast.makeText(this, "Bookmark (stub)", Toast.LENGTH_SHORT).show();
            });
            addSidebarOption("Reader / Document", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                // Future: extract main content via C++ / JS and show in document UI
                LogManager.i("Activity", "Reader mode framework triggered");
                Toast.makeText(this, "Reader mode (stub)", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void addSidebarOption(String label, LinearLayout.LayoutParams params,
                                  android.view.View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setLayoutParams(params);
        btn.setTextColor(Color.parseColor("#e8ecfb"));
        btn.setBackgroundColor(Color.parseColor("#1e2745"));
        btn.setAllCaps(false);
        btn.setOnClickListener(listener);
        drawerOptionsContainer.addView(btn);
    }

    private void initializeBrowserEngine() {
        if (host != null) return;

        host = new WebViewHost(this, webView);

        host.setUiCallback(new WebViewHost.BrowserUiCallback() {
            @Override
            public void onUrUpdated(String url) {
                runOnUiThread(() -> {
                    if (urlBar != null) {
                        // Keep omnibox empty on local welcome/settings surfaces
                        if (url != null && (url.startsWith("file://") || url.contains("/assets/ui/"))) {
                            urlBar.setText("");
                        } else {
                            urlBar.setText(url);
                        }
                        urlBar.clearFocus();
                    }
                });
            }

            @Override
            public void onProgressUpdated(int progress) {
                runOnUiThread(() -> {
                    if (progress >= 100) {
                        progressBar.setVisibility(android.view.View.GONE);
                    } else {
                        progressBar.setVisibility(android.view.View.VISIBLE);
                        progressBar.setProgress(progress);
                    }
                });
            }
        });

        host.attach();
        StateManager.get().transitionTo(BrowserState.WELCOME, null);
        LogManager.i("Activity", "BrowserActivity ready");
    }

    private String[] getRequiredPermissionsArray() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        return permissions.toArray(new String[0]);
    }

    private boolean hasRequiredPermissions() {
        for (String permission : getRequiredPermissionsArray()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else if (host != null && !host.goBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (host != null) {
            host.detach();
        }
        super.onDestroy();
    }
}
