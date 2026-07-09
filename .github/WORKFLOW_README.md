# CI/CD Pipeline Documentation

## Overview

This repository uses **GitHub Actions** to automatically test, build, and release your Android app.

## Workflows

### 1. **Android CI/CD Pipeline** (`android-test.yml`)
**Triggers:** On every push and pull request to `main` or `develop`

**What it does:**
- ✅ Compiles Java + C code
- ✅ Runs unit tests
- ✅ Builds debug APK
- ✅ Builds release APK (unsigned)
- ✅ Uploads APKs as artifacts
- ✅ Publishes test results

**Artifacts:**
- `debug-apk/` — Debug APK (7 days retention)
- `release-apk/` — Unsigned release APK (30 days retention)
- `test-results/` — HTML test reports (14 days retention)

---

### 2. **Auto Release** (`auto-release.yml`)
**Triggers:** When `android-test.yml` succeeds on `main` branch

**What it does:**
- 📦 Downloads release APK from build artifacts
- 🏷️ Creates a GitHub Release with version tag
- 📝 Attaches APK to the release

**Automatic Release Tag Format:** `v{versionName}-{timestamp}`

Example: `v0.0.1-20260709_101530`

---

### 3. **Failure Notification** (`notify-on-failure.yml`)
**Triggers:** When `android-test.yml` fails

**What it does:**
- 🚨 Logs failure details
- 📋 Provides link to workflow logs
- 💡 Lists common failure causes

---

## How to Use

### **Normal Development Flow**

1. **Push code to `main`:**
   ```bash
   git add .
   git commit -m "Fix app startup crash"
   git push origin main
   ```

2. **GitHub Actions automatically:**
   - Runs tests
   - Builds APK
   - If passing: Creates release
   - If failing: Shows error logs

3. **Check results:**
   - Go to **Actions tab** in GitHub
   - Click latest workflow run
   - View build logs and test results

---

### **Pull Request Workflow**

1. **Create a feature branch:**
   ```bash
   git checkout -b feature/my-fix
   ```

2. **Make changes and push:**
   ```bash
   git push origin feature/my-fix
   ```

3. **Open a pull request to `main`**
   - GitHub Actions tests your changes automatically
   - If ✅ passes: Ready to merge
   - If ❌ fails: Fix issues and re-push

---

## Common Failure Causes & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `UnsatisfiedLinkError` | Native library not built | Check CMakeLists.txt, run `./gradlew clean build` |
| `NullPointerException` | StateManager/LogManager initialization | Check that managers are initialized in AuroraApplication |
| `FileNotFoundException` | Missing `activity_browser.xml` or assets | Verify `res/layout/` and `assets/ui/` exist |
| `SIGSEGV` | C code crash | Check native code for buffer overflows, null pointers |
| `CompilationException` | Java syntax errors | Fix Java code, check imports |

---

## Viewing Test Results

1. Go to **Actions tab**
2. Click the latest workflow run
3. Scroll to **Publish test results** step
4. Click the result summary to view detailed report

---

## Manual Release (Without Auto-Release)

If you need to manually create a release:

1. Download the unsigned APK from the latest workflow run
2. Sign it with your keystore
3. Go to **Releases** tab
4. Click **Create a new release**
5. Upload the signed APK

---

## Configuring Automatic Signing (Advanced)

To auto-sign and release production APKs:

1. **Create a signing keystore** (if you don't have one):
   ```bash
   keytool -genkey -v -keystore release-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias release-key
   ```

2. **Encode to Base64:**
   ```bash
   base64 -i release-key.jks -o release-key.b64
   cat release-key.b64
   ```

3. **Add GitHub Secrets:**
   - Go to **Settings → Secrets and variables → Actions**
   - Add:
     - `RELEASE_KEYSTORE_BASE64` = (paste output from above)
     - `RELEASE_KEYSTORE_PASSWORD` = (your keystore password)
     - `RELEASE_KEY_ALIAS` = `release-key`
     - `RELEASE_KEY_PASSWORD` = (your key password)

4. **Update workflows to sign** (contact me for updated workflow files)

---

## Workflow Permissions

**Required GitHub Settings:**
- Go to **Settings → Actions → General**
- Set **Workflow permissions** to: **"Read and write permissions"**
- Enable **"Allow GitHub Actions to create and approve pull requests"**

---

## Troubleshooting

### **Workflow not running?**
- Check branch name (must be `main` or `develop`)
- Verify workflow file is in `.github/workflows/`
- Check **Actions** tab for disabled workflows

### **Build hangs or times out?**
- Default timeout: 45 minutes
- Edit `.github/workflows/android-test.yml` to increase `timeout-minutes`

### **Can't download artifacts?**
- Artifacts expire after retention days (7/14/30 based on type)
- Download within the retention period or rebuild

---

## Next Steps

1. ✅ Merge this branch into `main`
2. ✅ Verify workflows run on next push
3. ✅ Check Actions tab for results
4. ✅ Fix any test failures
5. ✅ Once passing: Auto-releases enabled!

---

## Questions?

View full workflow definitions:
- `.github/workflows/android-test.yml` — Build & test logic
- `.github/workflows/auto-release.yml` — Release logic
- `.github/workflows/notify-on-failure.yml` — Failure handling
