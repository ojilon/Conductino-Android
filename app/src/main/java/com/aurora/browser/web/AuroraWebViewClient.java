package com.aurora.browser.web;

import android.util.Log; //Added for debugging
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
//import android.webkit.WebViewClient;

import com.aurora.browser.core.NativeCore;
import com.aurora.browser.logging.LogManager;
import com.aurora.browser.net.ResourceRouter;

import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat; // instead of android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Intercepts every resource the WebView requests.
 *   
 */
public class AuroraWebViewClient extends WebViewClientCompat {

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();


        //Check if it is a custom rewritten scheme
        if (url.startsWith("aurora-local://")) {
            //Ask C fir the real file path
            String localPath = NativeCore.get().getLocalResourcePath(url);

            if (localPath != null) {
                File file = new File(localPath);
                if (file.exists()) {
                    try {
                        //Read the file from disk
                        InputStream inputStream = new FileInputStream(file);

                        //Determine MINE type (simplified for this example)
                        String mineType = "image/png";
                        if (uel.endstWith(".css")) mineType = "text/css";
                        else if (url.endstWith(".js")) mineType = "application/javascript";

                        //Return the local file directly to the Webview
                        return new WebResourceResponse(mineType, "UTF-8", inputStream);
                    }catch (FileNotFoundException e) {
                        LogManager.e("WebViewClientCompat", "Local file not found:" + localPath);
                    }
                                    
                }                
            }
        }

        //For all other web requests
        return super.shouldInterceptRequest(view, request);
    }
}
