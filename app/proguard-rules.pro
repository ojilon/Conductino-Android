# Keep the JS bridge intact (called by name from WebView JS).
-keepclassmembers class com.aurora.browser.api.BrowserBridge {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep native method signatures for JNI.
-keepclasseswithmembernames class com.aurora.browser.core.NativeCore {
    native <methods>;
}
