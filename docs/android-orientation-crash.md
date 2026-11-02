# Android orientation-provider crash

## Report

Reported on 1 November 2026 from Google Play crash vitals:

- Samsung Galaxy A10s, Android 11 (SDK 30).
- App 3.0.1, version code 1678468736.
- Sample occurred 11 days before the report.
- `IllegalStateException` in
  `AndroidOrientationProvider$1.invokeSuspend(AndroidOrientationProvider.kt:86)`.

The app used MapLibre Compose 0.13.1. Its
[provider at line 86](https://github.com/maplibre/maplibre-compose/blob/v0.13.1/lib/maplibre-compose/src/androidMain/kotlin/org/maplibre/compose/location/AndroidOrientationProvider.kt#L86)
throws `IllegalStateException("Rotation vector sensor is not available")` when
Android returns no `TYPE_ROTATION_VECTOR` sensor. This matches the reported stack
trace; the report does not include the exception message or a device sensor dump.

## Upgrade

Upgrade MapLibre Compose to 0.20.0. Its
[Android heading provider](https://github.com/maplibre/maplibre-compose/blob/v0.20.0/lib/location/src/androidMain/kotlin/org/maplibre/compose/location/AndroidHeadingProvider.kt)
completes its flow when the rotation-vector sensor is absent. Location tracking
and direction of travel remain available without compass heading. The app uses
`rememberLocationState`, which also handles heading-flow failures.

The migration replaces `CameraState` with `MapState`, declares sources and layers
in `rememberMapState`, and replaces `LocationPuck` with `LocationIndicatorLayer`.
Android explicitly uses the OpenGL runtime. iOS links the new native runtime
directly instead of obtaining MapLibre.framework through SwiftPM.

The new dependencies require compile SDK 37 and minimum Android SDK 24
(Android 7). The starting commit already uses minimum SDK 24. AGP is upgraded to
9.3.1 and Compose is aligned with MapLibre's 1.12.1 dependencies. Target SDK
remains 36.

## Verification

`AndroidHeadingProviderTest` runs at SDK 30 with no rotation-vector sensor and
asserts that heading collection completes without throwing. This reproduces the
missing-sensor condition rather than emulating every characteristic of a Galaxy
A10s. Check the map, parking markers/clusters, place search, location button,
permission grant/revocation, and location picker on real devices before release.

Validated on 1 November 2026:

- 89 Android host tests passed, including the SDK 30 missing-sensor regression.
- Both Android debug variants built; production R8 minification passed.
- 88 iOS simulator tests passed; the shared framework and complete Xcode simulator app built.
- Three native-symbol helper tests passed; release shell syntax and diff whitespace checks passed.
- Android emulator smoke check: base map, parking pins/clusters, and user location rendered;
  no `AndroidRuntime` crash was reported. The physical Galaxy A10s was not available.

Upstream [PR #1875](https://github.com/maplibre/maplibre-compose/pull/1875) covers
additional heading failures but merged after 0.20.0; this upgrade does not claim
to include that PR.

Native symbols are packaged from the bundle when available. Missing symbols warn
and do not block releases, as chosen for this upgrade; Kotlin/Java R8 mapping
remains required. See [Android releases](android-release.md).
