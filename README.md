# MyTasbih Android project

## Build APK on GitHub
1. Upload the contents of this folder to the root of `marat8820/MyTasbih`.
2. Keep the `.github/workflows/build.yml` path intact.
3. Open the repository's **Actions** tab. A push starts the build automatically.
4. Open the successful run and download the `MyTasbih-debug-apk` artifact.

## Package
The current Android package is `com.marat8820.mytasbih`. If you already published the app with a different application ID, keep the existing ID to preserve update compatibility.

## Notes
- This workflow builds a debug APK for testing, not a signed Play Store release.
- The app stores the counter and selected target locally.
- Reminder scheduling depends on Android battery/notification settings; Android can delay inexact alarms.
- This project uses Android Gradle Plugin 8.7.3, compile SDK 35 and Java 17.
