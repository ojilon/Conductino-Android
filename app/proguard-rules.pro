# Keep the JS bridge intact (called by name from WebView JS).
-keepclassmembers class com.conductino.study.api.BrowserBridge {
    @android.webkit.JavascriptInterface <methods>;
}
# Keep native method signatures for JNI.
-keepclasseswithmembernames class com.conductino.study.core.NativeCore {
    native <methods>;
}
