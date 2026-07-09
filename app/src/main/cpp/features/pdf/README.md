# features/pdf/

Native PDF handling. Recommended: **MuPDF** (small footprint) or **pdfium**.
Add `pdf_render.c` here, link the lib in CMake, expose `openPdf`/`renderPage`
through JNI, and pair with a `assets/ui/pdf/` viewer state.
