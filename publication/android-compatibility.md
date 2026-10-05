# Android 6 HTTPS compatibility

Minimum SDK: 23. The production API uses a Let's Encrypt certificate chain
ending at ISRG Root X1. Android 6 and early Android 7 system trust stores may
not contain this root (official compatibility starts at Android 7.1.1).

The Android API client adds the official ISRG Root X1 alongside the device's
existing trusted roots on API 23–25, only when using the production API URL.
Newer Android versions and local development use the default HTTP client.
Certificate-chain validation and OkHttp's default hostname validation remain
enabled. This does not modify the device's trust store or MapLibre's client.

Certificate source: https://letsencrypt.org/certs/isrgrootx1.pem

SHA-256: `96BCEC06264976F37460779ACF28C5A7CFE8A3C0AAE11A8FFCEE05C0BDDF08C6`

Compatibility reference: https://letsencrypt.org/docs/certificate-compatibility/

Verified on the Android API 23 emulator on 5 October 2026:

- Before the fix, the API failed with `Trust anchor for certification path not found`.
- With the additional root, a London bounds request returned HTTP 200 and 148 bays.
- The production debug app loaded and displayed parking markers and map tiles.
- A self-signed certificate and a certificate for the wrong hostname were rejected.
- MapLibre's style server worked with the default system trust store.

The emulator's `eglCodecCommon glUtilsParamSize` warning did not prevent map
rendering. Rebuild the signed production bundle before publishing; earlier
API 23 bundles do not contain this fix.
