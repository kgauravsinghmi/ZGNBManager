# ZGNBManager

ZGNBManager is an Android application for APK and archive analysis, file management, and reverse-engineering workflows. It is designed to help inspect APK/ZIP contents, browse DEX classes and methods, work with resources, and analyze native and extracted content in a single project-focused tool.

## Features

- APK and ZIP inspection and management
- DEX class, method, and field browsing
- Search for classes and methods within APKs
- Text and code viewing/editing support
- Native and Android resource-related tooling
- File handling utilities for extracted content
- Deobfuscation and mapping export scaffolding for reverse-engineering workflows

## Project structure

- `app/` — Android application module
- `app/src/main/` — Android source code, resources, and manifest
- `app/libs/` — bundled libraries used by the project
- `build.gradle` — root Gradle configuration
- `settings.gradle` — Gradle project settings
- `gradle.properties` — AndroidX and Gradle JVM settings
- `gradlew` / `gradlew.bat` — Gradle wrapper scripts

## Prerequisites

- Android Studio (current stable release recommended)
- JDK 17 or compatible Java version
- Android SDK with API 34 installed
- Android NDK (required by the native build configuration in the app module)
- Git

## Local Android Studio setup

To build this project successfully on a machine, set up the local Android SDK and NDK before opening the project in Android Studio.

1. Install Android Studio.
2. Install the Android SDK platform for API 34.
3. Install a compatible Android NDK version (for example, NDK 25.x).
4. If Android Studio does not auto-detect the SDK, create a `local.properties` file in the project root with your machine-specific paths:

   ```properties
   sdk.dir=E:/zengxianxi/androidsdk
   ndk.dir=E:/zengxianxi/androidsdk/ndk/25.2.9519653
   ```

   If your Android SDK uses a legacy `ndk-bundle` folder instead of a versioned folder, the fallback is:

   ```properties
   sdk.dir=E:/zengxianxi/androidsdk
   ndk.dir=E:/zengxianxi/androidsdk/ndk-bundle
   ```

5. Ensure the project is using JDK 17 in Android Studio.
6. Sync the project and let Gradle download dependencies.

## Building

1. Clone the repository:

   ```bash
   git clone https://github.com/kgauravsinghmi/ZGNBManager.git
   cd ZGNBManager
   ```

2. Open the project in Android Studio.

3. Let Gradle sync and download dependencies.

4. Ensure `local.properties` points to your Android SDK installation if it is not auto-detected.

5. Build the debug APK:

   ```bash
   ./gradlew assembleDebug
   ```

   On Windows:

   ```powershell
   gradlew.bat assembleDebug
   ```

6. Install the generated APK on an emulator or Android device.

## Notes

This project is intended for Android reverse-engineering, APK analysis, and software inspection workflows. Use it responsibly and only in compliance with applicable laws, platform policies, and software licensing requirements.

The project has been modernized for current Android tooling and uses AndroidX-based Gradle settings. Some legacy dependencies may still require cleanup depending on the target environment and local SDK setup.

## Contributing

Contributions are welcome. Please read `CONTRIBUTING.md` before opening a pull request or issue.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
