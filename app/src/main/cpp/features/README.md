# `cpp/features/` — future heavy modules (C backend)

Each folder is a placeholder for a self-contained native feature. Add the
sources + a recommended light/robust lib, then register the new `.c` files and
link the lib in the root `CMakeLists.txt`, and expose entry points via
`jni_bridge.c` + a matching `native` method in `NativeCore.java`.

| Folder      | Feature                | Recommended lib(s)                            |
|-------------|------------------------|-----------------------------------------------|
| `html/`     | Real HTML tokenizer    | **lexbor** (fast, MIT) or **gumbo-parser**    |
| `pdf/`      | PDF parsing/render     | **MuPDF** (compact) or **pdfium**             |
| `media/`    | Video/audio demux/dec  | **FFmpeg** (min build) / **dav1d** for AV1    |
| `text/`     | Text editing/indexing  | **PCRE2** (regex) + a rope/piece-table impl   |
| `image/`    | Image decode/resize    | **libjpeg-turbo**, **libwebp**, **stb_image** |
| `crypto/`   | Hashing / passwords    | **libsodium** or **BLAKE3**                   |
| `net/`      | Native HTTP/2/3 stack  | **nghttp2** / **ngtcp2** + **mbedTLS**        |
| `archive/`  | Zip/tar for downloads  | **libarchive** or **miniz**                   |

## How to add one (example: pdf)
1. Put lib under `cpp/third_party/mupdf/`, sources in `cpp/features/pdf/`.
2. Add files + `target_link_libraries` in `CMakeLists.txt`.
3. Declare `Java_..._NativeCore_openPdf` in `jni_bridge.c`.
4. Add `public native ... openPdf(...)` in `NativeCore.java`.
5. Add a `assets/ui/pdf/` UI state + `BrowserState.PDF`.
