# Android releases

Run commands below from the repository root.

## Symbols

`scripts/release-android.sh` verifies the embedded R8 mapping and uploads available native debug symbols to Google Play
for the uploaded bundle's version code, before assigning it to a track and committing the release.
It requires Python 3 in addition to the existing `curl`, `jq`, and `gcloud` tools.

The script extracts the R8 mapping directly from the AAB and saves it as `mapping.txt` beside the
bundle for local crash analysis. Missing or empty mappings stop the release before uploading the AAB.
Google Play automatically imports the embedded mapping; a separate mapping upload would be rejected.
See the [Google Play documentation](https://support.google.com/googleplay/android-developer/answer/9848633?hl=en).

MapLibre Compose 0.20.0 uses the Native FFI OpenGL runtime. Its published Android
libraries and matching release archives are stripped, so full MapLibre native symbols are
currently unavailable. Releases continue with a warning for each library without symbols.
Kotlin/Java crash deobfuscation through the embedded R8 mapping is unaffected; native map
crashes may lack function names and source lines.

The script packages the native symbols Gradle embedded in the same AAB, when available.
It validates their paths, checks that each file is nonempty and has a corresponding packaged
library, and saves `native-debug-symbols.zip` beside the bundle. It no longer downloads symbols
for the old Android SDK: those cannot match the new FFI runtime. If no native symbols are
available, it warns and skips the native symbol upload. Invalid symbol metadata still stops
the release. Upload failures for available symbols also stop the release.

To prepare available symbols locally without publishing:

```sh
python3 scripts/prepare-android-symbols.py androidApp/build/outputs/bundle/prodRelease/androidApp-prod-release.aab
```

The command prints the archive path on stdout, or nothing when no symbols are available.
Warnings and included filenames are printed to stderr.

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

