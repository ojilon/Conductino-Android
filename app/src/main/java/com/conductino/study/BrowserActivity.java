package com.conductino.study;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.conductino.study.content.PageContentHelper;
import com.conductino.study.downloads.DownloadStore;
import com.conductino.study.library.BookmarkStore;
import com.conductino.study.library.HistoryStore;
import com.conductino.study.logging.LogManager;
import com.conductino.study.net.NavigationController;
import com.conductino.study.state.BrowserState;
import com.conductino.study.state.StateManager;
import com.conductino.study.tabs.TabManager;
import com.conductino.study.tabs.TabSession;
import com.conductino.study.web.WebViewHost;

import java.util.ArrayList;
import java.util.List;

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
                    if (!isGranted) allGranted = false;
                }
                if (!allGranted) {
                    Toast.makeText(this, "Storage permissions are required for public export.", Toast.LENGTH_LONG).show();
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
            requestPermissionLauncher.launch(getRequiredPermissionsArray());
        }
    }

    private void setupNativeUI() {
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                NavigationController.get().handleInput(urlBar.getText().toString().trim());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(urlBar.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        btnHome.setOnClickListener(v -> goToWelcome(false));
        btnAddTab.setOnClickListener(v -> goToWelcome(true));
        btnMenu.setOnClickListener(v -> {
            populateSidebarOptions();
            drawerLayout.openDrawer(GravityCompat.END);
        });
    }

    private void goToWelcome(boolean createNewTab) {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        if (createNewTab) {
            TabSession session = TabManager.get().create();
            Toast.makeText(this, "Tab " + session.id + " (" + TabManager.get().count() + ")",
                    Toast.LENGTH_SHORT).show();
        }
        TabManager.get().active().currentUrl = "";
        TabManager.get().active().title = "New Tab";
        StateManager.get().transitionTo(BrowserState.WELCOME, null);
        if (urlBar != null) {
            urlBar.setText("");
            urlBar.clearFocus();
        }
    }

    private void openDownloads() {
        StateManager.get().transitionTo(BrowserState.DOWNLOADS, DownloadStore.get().listAsJson());
    }

    private void openHistory() {
        StateManager.get().transitionTo(BrowserState.HISTORY, HistoryStore.get().listAsJson(100));
    }

    private void openBookmarks() {
        StateManager.get().transitionTo(BrowserState.BOOKMARKS, BookmarkStore.get().listAsJson());
    }

    private void bookmarkCurrent() {
        String url = urlBar != null ? urlBar.getText().toString().trim() : "";
        if (url.isEmpty() && webView != null) {
            url = webView.getUrl() != null ? webView.getUrl() : "";
        }
        if (url.isEmpty() || url.contains("appassets.androidplatform.net")) {
            Toast.makeText(this, "Nothing to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = webView != null && webView.getTitle() != null ? webView.getTitle() : url;
        boolean ok = BookmarkStore.get().add(url, title);
        Toast.makeText(this, ok ? "Bookmarked" : "Already bookmarked", Toast.LENGTH_SHORT).show();
    }

    private void promptFindInPage() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Find in page");
        new AlertDialog.Builder(this)
                .setTitle("Find in page")
                .setView(input)
                .setPositiveButton("Find", (d, w) -> {
                    String q = input.getText().toString();
                    PageContentHelper.findInPage(webView, q);
                    Toast.makeText(this, "Finding…", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Clear", (d, w) -> PageContentHelper.clearFind(webView))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openReader() {
        if (webView == null) return;
        final String url = webView.getUrl() != null ? webView.getUrl() : "";
        final String title = webView.getTitle() != null ? webView.getTitle() : "Reader";
        if (url.contains("appassets.androidplatform.net")) {
            Toast.makeText(this, "Open a web page first", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Extracting…", Toast.LENGTH_SHORT).show();
        PageContentHelper.extractBodyText(webView, text -> runOnUiThread(() -> {
            String payload = PageContentHelper.readerPayloadJson(title, url, text);
            StateManager.get().transitionTo(BrowserState.DOCUMENT, payload);
        }));
    }

    private void populateSidebarOptions() {
        drawerOptionsContainer.removeAllViews();
        BrowserState current = StateManager.get().current();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);

        boolean chrome = current == BrowserState.WELCOME
                || current == BrowserState.SETTINGS
                || current == BrowserState.DOWNLOADS
                || current == BrowserState.HISTORY
                || current == BrowserState.BOOKMARKS
                || current == BrowserState.DOCUMENT;

        if (chrome) {
            addSidebarOption("New Tab", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                goToWelcome(true);
            });
            addSidebarOption("History", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                openHistory();
            });
            addSidebarOption("Bookmarks", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                openBookmarks();
            });
            addSidebarOption("Downloads", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                openDownloads();
            });
            addSidebarOption("Settings", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                StateManager.get().transitionTo(BrowserState.SETTINGS, null);
            });
        } else {
            addSidebarOption("Refresh", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                if (webView != null) webView.reload();
            });
            addSidebarOption("Find in page", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                promptFindInPage();
            });
            addSidebarOption("Bookmark", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                bookmarkCurrent();
            });
            addSidebarOption("History", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                openHistory();
            });
            addSidebarOption("Downloads", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                openDownloads();
            });
            addSidebarOption("Reader / Document", params, v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                openReader();
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
                    boolean localUi = url != null
                            && (url.startsWith("file://") || url.contains("appassets.androidplatform.net"));
                    if (urlBar != null) {
                        urlBar.setText(localUi ? "" : (url != null ? url : ""));
                        urlBar.clearFocus();
                    }
                    if (!localUi && url != null && !url.isEmpty()) {
                        TabManager.get().recordNavigation(url, null);
                        String title = webView != null ? webView.getTitle() : null;
                        HistoryStore.get().add(url, title);
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
        TabManager.get().active();
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
        if (host != null) host.detach();
        super.onDestroy();
    }
}
