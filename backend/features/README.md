# backend/features

Domain stubs for future native modules. Keep each folder small until needed.

| Folder   | Purpose                                      |
|----------|----------------------------------------------|
| text/    | HTML/text extract, highlight helpers         |
| pdf/     | Document open / page raster (heavy libs)     |
| media/   | Playback helpers                             |
| net/     | Extra network utilities beyond core fetch    |
| crypto/  | Hashing / simple crypto                      |
| image/   | Image decode / favicons                      |
| archive/ | Zip / tar helpers                            |

Add a short README in each folder before large code. Prefer structs + free functions.
