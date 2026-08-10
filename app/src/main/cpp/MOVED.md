The native core has moved to the repository root:

```
backend/
```

See `backend/README.md` and `FOUNDATION.md`.

`app/build.gradle` now points:

```gradle
externalNativeBuild {
    cmake {
        path file("../backend/CMakeLists.txt")
    }
}
```

You can delete this folder once any remaining local stubs are gone.
