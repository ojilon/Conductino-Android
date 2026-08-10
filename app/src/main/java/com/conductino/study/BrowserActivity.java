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
 * The single Activity that hosts the WebView. It does NOT contain browser
 * logic — it delegates to the specialized manager packages:
 *
 *   web/       -> owns the WebView + JS bridge
 *   state/     -> decides which assets/ui/<state> to render
 *   net/       -> fetch, stream-vs-download decisions
 *   api/       -> the JS <-> Java contract
 *   core/      -> JNI into the C backend
 *   settings/  -> settings.json
 *   logging/   -> LogManager
 */

public class BrowserActivity extends AppCompatActivity {

    private WebViewHost host;
    private WebView webView;
    private EditText urlBar;
    private ProgressBar progressBar;

    // New UI Elements
    private DrawerLayout drawerLayout;
    private ImageButton btnHome;
    private ImageButton btnAddTab;
    private ImageButton btnMenu;
    private LinearLayout drawerOptionsContainer;

    // Asynchronous launcher that handles the native permission dialog popup
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
                    initializeBrowserEngine();
                } else {
                    LogManager.i("Activity", "Permissions denied.");
                    Toast.makeText(this, "Storage permissions are required for downloads.", Toast.LENGTH_LONG).show();
                    // Optional: Close app or limit features if denied
                    initializeBrowserEngine(); 
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        //Bind all the UI elements (Original + New Native Chrome)
        drawerLayout = findViewById(R.id.drawer_layout);
        webView = findViewById(R.id.web_view);
        urlBar = findViewById(R.id.url_bar);
        btnHome = findViewById(R.id.btn_home);
        btnAddTab = findViewById(R.id.btn_add_tab);
        btnMenu = findViewById(R.id.btn_menu);
        progressBar = findViewById(R.id.progress_bar);
        drawerOptionsContainer = findViewById(R.id.drawer_options_container);

        //Setup listeners for the new native UI
        setupNativeUI();

        // Check if we need to prompt the user or if we can start immediately
        if (hasRequiredPermissions()) {
            initializeBrowserEngine();
        } else {
            LogManager.i("Activity", "Requesting permissions asynchronously...");
            requestPermissionLauncher.launch(getRequiredPermissionsArray());
        }
    }

    private void setupNativeUI() {
        //Listen for the "Go" / "Enter" key on the soft keyboard (Omnibox Setup)
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String input = urlBar.getText().toString().trim();
                LogManager.i("Activity", "User submitted imput via URL bar: " + input);
                NavigationController.get().handleInput(input); //hand off the url pasring

                //Hide the Keyboard for clean user experience
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(urlBar.getWindowToken(), 0);
                }

                return true;
            }
            return false;
        });

        //Back button -> transition back to Welcome State
        btnHome.setOnClickListener(v -> {
            LogManager.i("Activity", "Home button clicked");
            StateManager.get().transitionTo(BrowserState.WELCOME, null);
        });

        // Add tab Button
        btnAddTab.setOnClickListener(v -> {
            LogManager.i("Activity", "Add tab button clicked");
            Toast.makeText(this, "Tabs feature ready!", Toast.LENGTH_SHORT).show();
        });

        //Menu button -> Populate dynamic options and open drawer
        btnMenu.setOnClickListener(v -> {
            LogManager.i("Activity", "Menu button clicked");
            populateSidebarOptions();
            drawerLayout.openDrawer(GravityCompat.END);
        });
    }

    private void populateSidebarOptions() {
        // Clear old options to prevent duplicates
        drawerOptionsContainer.removeAllViews();
        BrowserState currentState = StateManager.get().current();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);

        // Dynamically add options based on current browser state
        if (currentState == BrowserState.WELCOME) {
            Button settingsBtn = createSidebarButton("Settings", params);
            settingsBtn.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                StateManager.get().transitionTo(BrowserState.SETTINGS, null);
            });
            drawerOptionsContainer.addView(settingsBtn);
        }else {
            Button refreshBtn = createSidebarButton("Refresh Page", params);
            refreshBtn.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                if (webView != null) webView.reload();
            });
            drawerOptionsContainer.addView(refreshBtn);

            Button documentBtn = createSidebarButton("Render Mode", params);
            documentBtn.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                LogManager.i("Activity", "Reader mode framework triggered");
                //Future C++ backend document rendering all go here
            });
            drawerOptionsContainer.addView(documentBtn);
        }
    }

    // Helper to generate programmatic sidebar buttons
    private Button createSidebarButton(String text, LinearLayout.LayoutParams params) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setLayoutParams(params);
        btn.setTextColor(Color.parseColor("#ebdbb2"));
        btn.setBackgroundColor(Color.parseColor("#3c3836"));
        btn.setAllCaps(false);
        return btn;
    }

    // Safely kicks off the UI and core engines once permissions are clear
    private void initializeBrowserEngine() {
        if (host == null) {
            host = new WebViewHost(this, webView);

            //Sync webview events back to the Native UI
            host.setUiCallback(new WebViewHost.BrowserUiCallback() {
                @Override
                public void onUrUpdated(String url) {
                    //Update the native url bar when a new page loads
                    runOnUiThread(() -> {
                        if (urlBar != null) {
                            urlBar.setText(url);
                            urlBar.clearFocus();
                        }
                    });
                }

                @Override
                public void onProgressUpdated(int progress) {
                    //Animate the native progress bar
                    runOnUiThread(() -> {
                        if (progress == 100) {
                            progressBar.setVisibility(android.view.View.GONE);
                        }else {
                            progressBar.setVisibility(android.view.View.VISIBLE);
                            progressBar.setProgress(progress);
                        }
                    });
                }
            });


            host.attach();

            // First paint: the welcome UI.
            StateManager.get().transitionTo(BrowserState.WELCOME, null);
            LogManager.i("Activity", "BrowserActivity ready");
        }
    }

    // Helper to determine what this specific device needs based on its Android version
    private String[] getRequiredPermissionsArray() {
        List<String> permissions = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else { // Android 12 and below
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // Android 10 and below
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
        //Intercept back press to close the sidebar of it's currently open
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }else if (host != null && !host.goBack()) {
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
