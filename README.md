# UK Motorcycle Parking

This is a Kotlin Multiplatform project targeting Android and iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with
  Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for
  your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform
  applications.
  It contains several subfolders:
    - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the
      folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part,
      the [jvmMain](./shared/src/jvmMain/kotlin)
      folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these
commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### API environments

The app selects its API automatically:

- Android debuggable builds and iOS debug binaries use the isolated development API at
  `http://127.0.0.1:8080/api_dev/`.
- Release builds use `https://melashkov.com/api/`.

The URLs are defined in
[ApiEnvironment.kt](./shared/src/commonMain/kotlin/com/melashkov/mcparking/di/ApiEnvironment.kt).
Android and iOS determine whether the running app is a debug build in their respective
`androidMain` and `iosMain` implementations.

### Bootstrap the local API

Set up the separate `motorcycle_parking_api` project using its README, including its PHP config
and MySQL database. Start its development server so that physical devices can reach it over the
local network:

```bash
cd ../motorcycle_parking_api
php -S 0.0.0.0:8080 -t html
```

For an Android device connected over USB, forward the development port before launching the app:

```bash
adb reverse tcp:8080 tcp:8080
```

The iOS Simulator can use the configured loopback address directly. For an iOS device, or an
Android device without USB forwarding, replace `127.0.0.1` with the Mac's LAN address. Connect the
Mac and phone to the same network and allow incoming PHP connections through the macOS firewall.

Find the Mac's current Wi-Fi address:

```bash
ipconfig getifaddr en0
```

If that address changes, update both:

- `DEVELOPMENT_API_BASE_URL` in
  [ApiEnvironment.kt](./shared/src/commonMain/kotlin/com/melashkov/mcparking/di/ApiEnvironment.kt).
- The allowed domain in
  [network_security_config.xml](./androidApp/src/debug/res/xml/network_security_config.xml).

Reinstall or rebuild the app after changing the address. A DHCP reservation for the Mac can keep
the development address stable.

Verify the API from the Mac before launching the app:

```bash
curl "http://127.0.0.1:8080/api_dev/bounds.php?north=51.52&south=51.50&east=-0.08&west=-0.11&limit=1"
```

For a physical device using the Mac's LAN address, opening the same URL in the device browser is a
useful final connectivity check. On iOS, accept the local-network access prompt when it appears.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
