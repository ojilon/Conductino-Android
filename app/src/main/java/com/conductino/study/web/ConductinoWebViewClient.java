package com.conductino.study.web;

import android.util.Log; //Added for debugging
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
//import android.webkit.WebViewClient;

import com.conductino.study.core.NativeCore;
import com.conductino.study.logging.LogManager;

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
public class ConductinoWebViewClient extends WebViewClientCompat {
    private final WebViewAssetLoader assetLoader;

    public ConductinoWebViewClient(WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

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

                        //Determine MIME type (simplified for this example)
                        String mimeType = "image/png";
                        if (url.endsWith(".css")) mimeType = "text/css";
                        else if (url.endsWith(".js")) mimeType = "application/javascript";

                        //Return the local file directly to the Webview
                        return new WebResourceResponse(mimeType, "UTF-8", inputStream);
                    }catch (FileNotFoundException e) {
                        LogManager.e("WebViewClientCompat", "Local file not found:" + localPath, e);
                    }
                                    
                }                
            }
        }

        // Let the assetLoader try to serve the file from local assets
        WebResourceResponse assetResponse = assetLoader.shouldInterceptRequest(request.getUrl());
        if (assetResponse != null) {
            return assetResponse;
        }

        //For all other web requests
        return super.shouldInterceptRequest(view, request);
    }
}
