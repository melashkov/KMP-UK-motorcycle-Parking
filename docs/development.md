# Development

Run commands below from the repository root.

Kotlin Multiplatform with shared Compose UI and business logic. MapLibre provides the
maps, Ktor handles API requests, and Koin handles dependency injection.

| Directory | Contents |
| --- | --- |
| [shared/src/commonMain](../shared/src/commonMain) | Shared UI and application logic |
| [shared/src/androidMain](../shared/src/androidMain) | Android implementations |
| [shared/src/iosMain](../shared/src/iosMain) | iOS implementations |
| [androidApp](../androidApp) | Android app and build configuration |
| [iosApp](../iosApp) | Xcode project and Swift entry point |

### Android

Open the repository in Android Studio and sync Gradle. CI uses JDK 21 and the
project compiles against Android SDK 37.

Select `debug` in **View → Tool Windows → Build Variants**, then run `androidApp`.
By default this uses the production API and works on an emulator or physical device.

To build an APK from the command line:

```sh
./gradlew :androidApp:assembleDebug
```

### iOS

On an Apple Silicon Mac, open [iosApp/iosApp.xcodeproj](../iosApp/iosApp.xcodeproj)
in Xcode and run the `iosApp` scheme. CI uses Xcode 26.3; the deployment target is iOS 18.2.

The Java path in [Config.xcconfig](../iosApp/Configuration/Config.xcconfig) points to
Android Studio's bundled JDK. Adjust it if your installation is elsewhere.

Both platforms default to the production API. Release builds always use production.

### Local mock API

Contributors can run a synthetic API without access to the private backend. It needs
only Python 3, with no packages to install:

```sh
python3 scripts/mock-api.py
```

In a debug app, open **Settings → Debug API → Local development**, then stop and
run the app again from Android Studio or Xcode so the app process restarts. Settings
shows the active backend and the choice for the next launch. Reports and feedback
are disabled while the two choices differ. Switching back to Production also
requires a restart.
Android uses `http://10.0.2.2:8080/api/` (the emulator's host alias); the iOS simulator
uses `http://127.0.0.1:8080/api/`. The server binds only to the computer's loopback
interface. Local development currently supports the Android emulator and iOS
simulator.

Each valid bounds search returns 10 randomly positioned, clearly labelled mock bays
inside the requested area. Identical bounds produce identical bays, making repeated
checks reproducible. Edit `scripts/mock-api.py` to change the sample data. Place
search returns a synthetic London result; reports and feedback return success but
are discarded. Nothing is forwarded to production or saved. Map tiles still need
internet access. Stop the server with Ctrl+C.

### Tests

Run from the repository root. iOS tests require an Apple Silicon Mac and Xcode.

```sh
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
python3 -m unittest discover -s scripts/tests -v
```

The [Tests and builds workflow](../.github/workflows/build.yml) runs shared tests,
builds the Android debug app, and builds the iOS simulator app on pushes and
pull requests to `main`. Test reports, APKs and the simulator app are saved as artifacts.

### Android releases

```sh
./gradlew :androidApp:bundleRelease
```

Signing requires `RELEASE_KEYSTORE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`
and `RELEASE_KEY_PASSWORD`. See the [Android release guide](./android-release.md)
for the publishing script, crash mappings and native symbols.

