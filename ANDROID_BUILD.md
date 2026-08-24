# COLORJET Management Suite - Android Build

This guide documents how the Android build system compiles and packagers the final production application.

## Environment Specifications

- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`)
- **Android Gradle Plugin (AGP):** Latest stable release matching Kotlin Compose compiler.
- **Signing Keystore:** `my-upload-key.jks` / `debug.keystore`

## Building Command Log

To compile and package the Android installation files:

### 1. Build Debug APK
For rapid testing and client inspection:
```bash
gradle :app:assembleDebug
```
*Outputs file at: `app/build/outputs/apk/debug/app-debug.apk`*

### 2. Build Release AAB
For uploading directly to Google Play Store Console:
```bash
gradle :app:bundleRelease
```
*Outputs file at: `app/build/outputs/bundle/release/app-release.aab`*

---

*Note: All package sign-offs require correct environment keystore credentials (`STORE_PASSWORD`, `KEY_PASSWORD`) populated inside the environment properties list or local vault store.*
