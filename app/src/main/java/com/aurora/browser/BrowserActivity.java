package com.aurora.browser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aurora.browser.logging.LogManager;
import com.aurora.browser.state.BrowserState;
import com.aurora.browser.state.StateManager;
import com.aurora.browser.web.WebViewHost;
import com.aurora.browser.net.NavigationController;

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

    //Native UI Elements
    private EditText urlBar;
    private ImageButton btnBack;
    private ImageButton btnMenu;
    private ProgressBar progressBar;

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
        webView = findViewById(R.id.web_view);
        urlBar = findViewById(R.id.url_bar);
        btnBack = findViewById(R.id.btn_back);
        btnMenu = findViewById(R.id.btn_menu);
        progressBar = findViewById(R.id.progress_bar);

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
        //Listen for the "Go" / "Enter" key on the soft keyboard
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

        //Wire up the Native Back button
        btnBack.setOnClickListener(v -> {
            LogManager.i("Activity", "Native back button clicked");
            onBackPressed();
        });

        //Menu button
        btnMenu.setOnClickListener(v -> {
            LogManager.i("Activity", "Menu button clicked");
            // StateManager.get().transitionTo(BrowserState.SETTINGS, null);
        });
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
        if (host != null && !host.goBack()) {
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
