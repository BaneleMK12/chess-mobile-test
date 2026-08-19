# Kotlin Chess Mobile Test

A native Android chess game written in Kotlin. The app renders a playable two-player chess board, highlights legal moves, supports captures and pawn promotion to queen, and declares the winner when a king is captured.

## Build locally

```bash
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Test locally

```bash
gradle testDebugUnitTest
```

## GitHub Actions

The Android APK workflow runs unit tests, builds the debug APK, uploads it as an artifact, and performs an emulator smoke test that installs and launches the APK.
