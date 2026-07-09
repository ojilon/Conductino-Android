# features/image/

Fast image decode + downscale before handoff to the WebView. Recommended:
**libjpeg-turbo**, **libwebp**, and **stb_image** for odd formats. Keeps large
bitmaps off the JVM heap.
