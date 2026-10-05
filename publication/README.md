# UK Motorcycle Parking — publication preparation

Prepared 4 October 2026; Android screenshot set updated 5 October 2026. Locale: English (UK).

## Status

The store text, Google Play feature graphic, icon and **eight Android store screenshots** are prepared. The Android images were rendered from the ten captures supplied by the user on 5 October. **iPhone and iPad screenshot sets remain incomplete.** Nothing has been uploaded or submitted to either store. App release code has not been edited by the publication work. Security work is committed separately; the publication branch does not include the security implementation.

Download `google-play-screenshots.zip` for the eight upload images in order. `android-screenshot-preview.jpg` is a contact sheet for review only; do not upload it as a phone screenshot. The first six slides cover distinct features; the final two show additional dark-mode screens.

## Copy and branding

Use `en-GB/store-copy.md` to review all fields together. Individual plain-text fields are in `en-GB/android/` and `en-GB/ios/` for copying into the consoles. Both stores use the name **UK Motorcycle Parking**, the same full description and the same release notes.

The copy reflects the current source: map browsing, visible-area search, current location, parking categories, details, external directions, sharing, Street View links, reviewed additions/corrections, and light/dark appearance. Street/city/postcode search is explicitly described as temporarily unavailable. Offline operation, real-time availability, automatic routing, universal coverage, and unverified location counts are not advertised.

Google Play: title 21/30; short description 74/80; full description 1409/4000; release notes 424/500.
Apple: name 21/30; subtitle 27/30; promotional text 141/170; description 1409/4000; keywords 80/100; release notes 424/4000.

`google-play-feature-graphic.png`: 1024 × 500, RGB PNG.
`google-play-icon.png`: 512 × 512, RGB PNG, exported from the existing app artwork.

## Screenshot design and upload order

The design uses a navy background, clear white benefit headlines and real app captures. Preserve map attribution, app labels and complete screenshots; do not invent app screens or copy Android captures into iOS frames.

| Order | Headline | Capture |
| --- | --- | --- |
| 1 | Find a bay. Park your bike. | Light map |
| 2 | Check the bay. Plan your stop. | Light bay details and actions |
| 3 | Explore somewhere new. | Search this area |
| 4 | Found a bay? Put it on the map. | Light add-bay form |
| 5 | Help keep parking up to date. | Light suggested-edit form |
| 6 | A map that fits your day. | Dark map |
| 7 | The details. In dark mode. | Dark bay details and actions |
| 8 | Share a bay. In either theme. | Dark add-bay form |

| Export | Size | Current status |
| --- | --- | --- |
| Android phone | 1080 × 1920 | Eight complete RGB PNG exports |
| iPhone 6.9-inch | 1320 × 2868 | Genuine iPhone captures pending |
| iPad 13-inch | 2064 × 2752 | Genuine iPad captures pending |

The full app captures are scaled proportionally into the navy store layout. Their UI, theme, text, data, attribution and camera cutout are retained. The supplied originals are 1080 × 2400 RGBA images; upload exports are 1080 × 1920 RGB PNGs without transparency.

`render-assets.py` contains the ordered screen names and captions and reads the matching captures under `screenshots/raw/<device>/`. `asset-manifest.json` lists the generated store exports. All ten supplied originals are retained: the dark suggested-edit and Behind the app captures are alternatives, not included in the eight-slide upload ZIP. Initial diagnostic/error captures are not upload assets.

## Capture provenance

The current Android set uses user-supplied captures from 5 October 2026. `capture-provenance.json` maps each saved original to its attachment filename. The platform identification is based on the Android status bar and emulator camera cutout, consistent with the earlier Android emulator capture. The exact build version was not independently verified. Confirm the screenshots match the final release build before submission.

The original isolated-build dark-map capture from 4 October is retained at `screenshots/raw/android/04-dark-map.png` for historical provenance; it is no longer a current store export.

During preparation on 4 October, the security-enabled iOS simulator build compiled successfully but its API requests encountered a missing production security endpoint. An isolated committed iOS build failed in the MapLibre Swift-package dependency build. No iPhone/iPad marketing screenshots are marked complete. These are historical checks, not a fresh report of today's server state.

## Release blockers and checks

1. **Deploy the installation-security API before releasing the security-enabled app build.** On 4 October, `https://melashkov.com/api/security.php` returned HTTP 404. The security branch calls this before requesting parking bays, producing an error instead of loaded parking. The existing bounds endpoint works. Follow the API project's installation-security guide and verify final production builds on physical devices.
2. **Use the existing iOS bundle identifier.** Apple's public lookup for app 6451344073 reports `com.melashkov.UK-Motorcycle-Parking` (version 1.2). The project currently uses `com.melashkov.mcparking.UKMotorcycleParking$(TEAM_ID)`. These are different. Confirm the identifier and signing team in App Store Connect before creating an update; do not create a second app by accident. This preparation did not change the identifier.
3. **Advance version and build numbers.** Android currently has version name 3.0 and version code 1678468729; confirm the next code exceeds the highest uploaded build. iOS configuration currently specifies marketing version 1.0 and build 1; the public iOS version is 1.2. Choose the next version and an unused build number before uploading. The marketing text does not hard-code a version.
4. **Complete the iOS screenshot sets.** iPad is supported by the current target and needs its own genuine captures. Review every final image for loaded maps, correct platform UI, visible attribution and absence of errors.
5. **Review privacy declarations against the final build.** The old listings describe different privacy practices. Installation keys/attestation, requests to the parking API and map providers, optional location access, and external links should be reviewed against the deployed implementation. Do not simply copy old declarations. Confirm support and privacy links still work.
6. **Verify the release build.** Test nearby location, map refresh, bay details, directions, sharing, Street View, themes, and reviewed report/edit submissions using controlled test data. Ensure production signing and installation verification are configured for both stores.

## Official references

- Existing Android listing: https://play.google.com/store/apps/details?id=com.melashkov.mcparking&hl=en_GB
- Existing iOS listing: https://apps.apple.com/gb/app/uk-motorcycle-parking/id6451344073
- Public iOS identifier lookup: https://itunes.apple.com/lookup?id=6451344073&country=gb
- Apple screenshot sizes: https://developer.apple.com/help/app-store-connect/reference/app-information/screenshot-specifications
- Apple product-page fields: https://developer.apple.com/app-store/product-page/
- Google Play text limits: https://support.google.com/googleplay/android-developer/answer/9859152
- Google Play graphic and screenshot requirements: https://support.google.com/googleplay/android-developer/answer/9866151?hl=en

The asset sizes and text limits were checked against the official documentation during preparation.
