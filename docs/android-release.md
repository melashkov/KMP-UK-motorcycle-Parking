# Android releases

Run commands below from the repository root.

## Symbols

`scripts/release-android.sh` verifies the embedded R8 mapping and uploads native debug symbols to Google Play
for the uploaded bundle's version code, before assigning it to a track and committing the release.
It requires Python 3 in addition to the existing `curl`, `jq`, and `gcloud` tools.

The script extracts the R8 mapping directly from the AAB and saves it as `mapping.txt` beside the
bundle for local crash analysis. Missing or empty mappings stop the release before uploading the AAB.
Google Play automatically imports the embedded mapping; a separate mapping upload would be rejected.
See the [Google Play documentation](https://support.google.com/googleplay/android-developer/answer/9848633?hl=en).

MapLibre ships stripped native libraries, so the script downloads its matching OpenGL **release**
symbol archive from the official MapLibre GitHub release. The archive is cached under
`build/native-symbols/`, checked against its SHA-256 checksum, and each library's ELF build ID is
matched against the AAB. Missing or mismatched symbols stop the release. Gradle also includes any
available full debug symbols from other dependencies; symbols already removed by their publishers
cannot be recovered (including the current AndroidX graphics path library).

When upgrading MapLibre, update both `mapLibreAndroidVersion` and
`mapLibreAndroidSymbolsSha256` in `gradle.properties`. Use the checksum for
`debug-symbols-maplibre-android-opengl-release-android-v<VERSION>.tar.gz` from the corresponding
[MapLibre release](https://github.com/maplibre/maplibre-native/releases).

To prepare and verify symbols locally without publishing:

```sh
python3 scripts/prepare-android-symbols.py androidApp/build/outputs/bundle/prodRelease/androidApp-prod-release.aab
```

The resulting `native-debug-symbols.zip` is saved beside the AAB. An already downloaded archive can
be supplied with `--archive /path/to/archive.tar.gz`; the same checksum and build ID checks apply.

## Release builds

Android release builds enable R8 code optimization and resource shrinking. Preview tooling is
available in debug builds; the app's preview annotations are compile-only. R8 is pinned in
`settings.gradle.kts` to a version that supports the project's Kotlin 2.4 metadata.

The shared UI uses the Material Icons library; R8 removes unused icon classes from Android release
builds. Unused shared drawable assets have been removed.

Release builds generate `androidApp/build/outputs/mapping/prodRelease/mapping.txt`; Gradle also
embeds this mapping in the AAB for Google Play to deobfuscate crashes. The release script verifies
the embedded mapping and relies on Play to import it from the bundle. Keep the mapping for the
exact build when analyzing crashes locally. Test map loading, parking details, the parking report
form, and settings on an optimized release build when changing dependencies or keep rules.

