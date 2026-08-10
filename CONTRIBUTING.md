# Contributing to Conductino / Aurora

Thanks for contributing! This document explains how to get the project running locally, how to open high-quality issues and pull requests, and what we expect from contributors.

Table of contents
- Getting started (quick)
- Local development setup
- Building the app (Java + native core)
- Tests
- Reporting issues
- Submitting a pull request
- Code style & review checklist
- Adding new features & docs
- License & trademarks

Getting started (quick)
1. Fork the repo and clone your fork.
2. Install Android SDK command-line tools and Android NDK r26+.
3. Use the Gradle wrapper for all builds: `./gradlew assembleDebug`.
4. Run unit tests: `./gradlew :app:testDebugUnitTest`.

Local development setup
- Java/Android
  - Install Android SDK command-line tools and create an SDK layout (sdkmanager etc.).
  - Android Studio is optional but convenient for debugging and emulators.
  - Use the project-provided wrapper: `./gradlew` (Gradle 8.x compatible).

- Native (optional)
  - Install Android NDK r26 or newer.
  - If you plan to build the native core, install CMake and ensure `cmake` is on PATH or available to Android Studio.
  - Native third-party libraries (SQLite amalgamation, curl, lexbor, etc.) are NOT committed. Place them under `backend/third_party/` as explained in `backend/README.md` before enabling the native build.

Building the app
- Java-only (no native core):

  ./gradlew assembleDebug

  This will build the Android app in the typical debug configuration. The wrapper ensures reproducible Gradle version.

- With native core (C/C++ via CMake)
  1. Populate `backend/third_party/` with required native libraries (see `backend/README.md`).
  2. Enable the build flag: add `-DBUILD_AURORA_CORE=ON` to `externalNativeBuild.cmake.arguments` in `app/build.gradle` or pass it via Gradle properties.
  3. Run the same Gradle assemble step; Gradle will invoke CMake to build the native library.

Tests
- Unit tests (JVM/Robolectric):

  ./gradlew :app:testDebugUnitTest

  - Unit tests sit under `app/src/test`. They prefer pure logic (stores, managers) and use Robolectric only when Context/assets are required.

- Instrumented tests (device/emulator):

  ./gradlew :app:connectedDebugAndroidTest

  - These tests require an emulator or connected device.

- Native tests
  - If you add native unit tests, document how to run them in the relevant `backend/` feature README.

Reporting issues
- Before opening an issue
  - Search existing issues to avoid duplicates.
  - Reproduce the problem on the latest main branch.

- Include in every bug report
  - A short descriptive title.
  - Steps to reproduce (exact commands, emulator/device, API level).
  - Expected vs actual behavior.
  - Logs: `adb logcat` and Gradle build output if relevant.
  - Attach screenshots or small recordings where helpful.

- Feature requests
  - Describe user-facing behavior and an example workflow.
  - If applicable, propose a small, incremental implementation plan.

Submitting a pull request
- Work on a branch named like: `feat/<short-desc>` or `fix/<short-desc>`.
- Keep PRs small and focused — prefer multiple small PRs rather than one large change.
- Rebase on top of main before opening the PR to avoid merge conflicts.
- Provide a clear description of what changed and why, including links to related issues.
- Add tests for new behavior where practical.
- If your change touches native and Java layers, split the changes when feasible (native build + Java wiring) to make reviews easier.

Code style & review checklist
- Java
  - Follow standard Android/Kotlin/Java conventions used in the project.
  - Keep methods small and focused; avoid large Activities.

- C/C++ (backend)
  - Prefer structs, enums, and free functions; keep JNI surface minimal.
  - Add small README to a new feature folder before adding large native code.

- Commits
  - Use imperative, lower-case commit messages with a short prefix when appropriate (e.g., `fix:`, `docs:`, `feat:`).
  - Keep commits focused and self-contained.

- PR Review checklist (include in PR description if helpful):
  - [ ] Does the change have a clear, testable purpose?
  - [ ] Are new or changed behaviors covered by tests or a manual test plan?
  - [ ] Is the changelog / README / docs updated if user-visible behavior changed?
  - [ ] Native dependencies listed and instructions present if needed.

Adding new features & docs
- Add a short README.md in any new top-level feature directory describing purpose and recommended libraries.
- Document how to build and test the added feature (Java and native, if applicable).
- For UI changes, include screenshots or recordings in the PR when possible.

CI and automation
- The project uses the Gradle wrapper to ensure deterministic builds.
- CI will run build and tests on PRs; ensure tests pass locally before opening a PR.

Security & responsible disclosure
- If you discover a security vulnerability, please do not open a public issue. Contact the maintainers directly (via email in the repo profile) with details so it can be handled privately.

Communication
- Prefer concise, constructive comments in PRs and issues.
- If you’re proposing a large design change, open an issue describing the proposal first to gather feedback.

License & trademarks
- Contributions will be accepted under the repository's license (add a `LICENSE` file at the repo root if not present).
- If you are contributing third-party code, ensure it is compatible with the repo license and clearly document its origin and license in `backend/third_party/` or the relevant folder.

Thank you for contributing — we appreciate small, well-documented improvements. If you'd like, I can open a PR that adds this CONTRIBUTING.md on a branch instead of committing directly to main.