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

- Android local API build: `./gradlew :androidApp:assembleDevDebug`
- Android production API build: `./gradlew :androidApp:assembleProdDebug`
- Android release build: `./gradlew :androidApp:bundleProdRelease`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### API environments

Android has three supported build variants:

| Build variant | API | Application ID | Use it for |
|---|---|---|---|
| `devDebug` | Local API | `com.melashkov.mcparking.dev` | Developing against the API running on your Mac |
| `prodDebug` | `https://melashkov.com/api/` | `com.melashkov.mcparking.dev` | Testing production with debugger and developer tools |
| `prodRelease` | `https://melashkov.com/api/` | `com.melashkov.mcparking` | Play Store release builds |

Both Android debug variants use the `.dev` application ID suffix, so you can switch between local
and production APIs without replacing the Play Store app. Only `prodRelease` uses the production
application ID. A development release variant is disabled to prevent accidentally shipping a build
configured for the local API.

#### Switching API in Android Studio

1. Run **File → Sync Project with Gradle Files** after first pulling the flavor configuration.
2. Open **View → Tool Windows → Build Variants**.
3. Find the `androidApp` module and select `devDebug` for the local API or `prodDebug` for the
   production API.
4. Run the existing `androidApp` run configuration normally.

The toolbar dropdown selects a run configuration, not an API environment. The active entry in the
**Build Variants** window determines which API the app uses. Switching between `devDebug` and
`prodDebug` replaces the installed `.dev` app because both variants have the same application ID.

The local PHP server is required only for `devDebug`:

- On the Android emulator, the app uses `http://10.0.2.2:8080/api/`.
- On a physical Android device connected over USB, the app uses
  `http://127.0.0.1:8080/api/` and requires `adb reverse tcp:8080 tcp:8080`.
- `prodDebug` and `prodRelease` connect directly to production; do not start or forward the local
  server for these variants.

iOS continues to select its environment from the Xcode build: debug builds use
`http://127.0.0.1:8080/api/`, while release builds use production.

The production URL is defined in
[ApiUrlProvider.kt](./shared/src/commonMain/kotlin/com/melashkov/mcparking/di/ApiUrlProvider.kt),
alongside the two development URLs. Each platform implementation selects the appropriate
environment; `NetworkModule` creates the provider as an application singleton, while the HTTP
client only depends on `ApiUrlProvider.baseUrl`.

### Bootstrap the local API

Set up the separate `motorcycle_parking_api` project using its README, including its PHP config
and MySQL database. Start its development server on the Mac:

```bash
cd ../motorcycle_parking_api
php -S 127.0.0.1:8080 -t html
```

The Android emulator reaches that server through `10.0.2.2`; it does not need port forwarding.
For a physical Android device connected over USB, forward the device's loopback port before
launching the app:

```bash
adb reverse tcp:8080 tcp:8080
```

Confirm the forwarding rule with:

```bash
adb reverse --list
```

The iOS Simulator can use `127.0.0.1` directly. A physical iOS device cannot use the Mac's
loopback address; for that case, bind PHP to all interfaces and configure the iOS development URL
to use the Mac's LAN address:

```bash
php -S 0.0.0.0:8080 -t html
ipconfig getifaddr en0
```

Connect the Mac and iPhone to the same network and allow incoming PHP connections through the
macOS firewall. A DHCP reservation for the Mac can keep that development address stable.

Verify the API from the Mac before launching the app:

```bash
curl "http://127.0.0.1:8080/api/bounds.php?north=51.52&south=51.50&east=-0.08&west=-0.11&limit=1"
```

On a physical iOS device, opening the LAN-address version of the same URL in Safari is a useful
final connectivity check. Accept the local-network access prompt when it appears.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
