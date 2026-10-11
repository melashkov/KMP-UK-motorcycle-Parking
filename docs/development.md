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
project compiles against Android SDK 36.

Select `prodDebug` in **View → Tool Windows → Build Variants**, then run `androidApp`.
This uses the production API and works on an emulator or physical device.

To build an APK from the command line:

```sh
./gradlew :androidApp:assembleProdDebug
```

### iOS

On an Apple Silicon Mac, open [iosApp/iosApp.xcodeproj](../iosApp/iosApp.xcodeproj)
in Xcode and run the `iosApp` scheme. CI uses Xcode 26.3; the deployment target is iOS 18.2.

The Java path in [Config.xcconfig](../iosApp/Configuration/Config.xcconfig) points to
Android Studio's bundled JDK. Adjust it if your installation is elsewhere.

iOS uses the production API in both debug and release builds.

### Tests

Run from the repository root. iOS tests require an Apple Silicon Mac and Xcode.

```sh
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

The [Tests and builds workflow](../.github/workflows/build.yml) runs shared tests,
builds both Android debug variants, and builds the iOS simulator app on pushes and
pull requests to `main`. Test reports, APKs and the simulator app are saved as artifacts.

### Android releases

```sh
./gradlew :androidApp:bundleProdRelease
```

Signing requires `RELEASE_KEYSTORE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`
and `RELEASE_KEY_PASSWORD`. See the [Android release guide](./android-release.md)
for the publishing script, crash mappings and native symbols.

