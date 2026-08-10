# backend/ — native core

All C / future C++23 business logic lives here, **outside** `:app`.

```
backend/
  CMakeLists.txt          ← entry point (referenced from app/build.gradle)
  cmake/
    CompilerFlags.cmake   ← C++23 + strict warnings (enable when .cpp arrives)
  include/
    aurora_core.h         ← public C API
  src/
    jni_bridge.c          ← only file that talks to the JVM
    core.c
    storage.c
    network.c
    html_parser.c
  features/               ← per-domain stubs (text, pdf, media, …)
  third_party/            ← external libs (NOT committed; see .gitignore)
```

## Build switch

`BUILD_AURORA_CORE` defaults to **OFF** so a clean checkout builds without
`third_party/` populated. When you are ready:

1. Place sqlite amalgamation, curl, lexbor under `backend/third_party/`.
2. Pass `-DBUILD_AURORA_CORE=ON` via `app/build.gradle` `externalNativeBuild.cmake.arguments`.

## Conventions (from project goals)

- Prefer **structs, enums, free functions, namespaces** over classes.
- Keep JNI surface thin; heavy work stays in pure C/C++.
- New domains get a folder under `features/` and a short README before large code.
- clangd / LSP: open the repo root; CMake generate will expose include paths.

## App wiring

`app/build.gradle` → `externalNativeBuild.cmake.path = file("../backend/CMakeLists.txt")`
