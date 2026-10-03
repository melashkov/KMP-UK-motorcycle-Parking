#!/usr/bin/env bash

set -euo pipefail

# -----------------------------------------------------------------------------
# Arguments
# -----------------------------------------------------------------------------

ANDROID_MODULE="${1:-}"
PACKAGE_NAME="${2:-}"
KEYSTORE_PATH="${3:-}"
SERVICE_ACCOUNT_JSON="${4:-}"
KEY_ALIAS="${5:-}"
TRACK="${6:-internal}"

if [[ -z "$ANDROID_MODULE" ||
      -z "$PACKAGE_NAME" ||
      -z "$KEYSTORE_PATH" ||
      -z "$SERVICE_ACCOUNT_JSON" ||
      -z "$KEY_ALIAS" ]]; then

    echo "Usage:"
    echo
    echo "  $0 <android-module> <package-name> <keystore.jks> <service-account.json> <key-alias> [track]"
    echo
    echo "Example:"
    echo
    echo "  $0 composeApp com.example.app ~/keys/upload-keystore.jks ~/keys/play.json upload"
    echo
    echo "Track defaults to: internal"
    exit 1
fi

# Remove optional leading ":" from Gradle module name.
ANDROID_MODULE="${ANDROID_MODULE#:}"

# Convert Gradle module path:
#   apps:android -> apps/android
MODULE_DIR="${ANDROID_MODULE//:/\/}"

# -----------------------------------------------------------------------------
# Locate project
# -----------------------------------------------------------------------------

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

# -----------------------------------------------------------------------------
# Validate
# -----------------------------------------------------------------------------

if [[ ! -f "$KEYSTORE_PATH" ]]; then
    echo "ERROR: Keystore does not exist:"
    echo "  $KEYSTORE_PATH"
    exit 1
fi

if [[ ! -f "$SERVICE_ACCOUNT_JSON" ]]; then
    echo "ERROR: Service account JSON does not exist:"
    echo "  $SERVICE_ACCOUNT_JSON"
    exit 1
fi

if [[ ! -x "./gradlew" ]]; then
    echo "ERROR: ./gradlew not found."
    echo "Run this script from a project containing the Gradle wrapper."
    exit 1
fi

for cmd in curl jq gcloud; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "ERROR: '$cmd' is required but not installed."
        exit 1
    fi
done

# -----------------------------------------------------------------------------
# Read signing password
# -----------------------------------------------------------------------------

read -r -s -p "Upload keystore password: " KEYSTORE_PASSWORD
echo

export RELEASE_KEYSTORE="$KEYSTORE_PATH"
export RELEASE_STORE_PASSWORD="$KEYSTORE_PASSWORD"
export RELEASE_KEY_PASSWORD="$KEYSTORE_PASSWORD"
export RELEASE_KEY_ALIAS="$KEY_ALIAS"

# Google Application Default Credentials will read this file directly.
export GOOGLE_APPLICATION_CREDENTIALS="$SERVICE_ACCOUNT_JSON"

cleanup() {
    unset RELEASE_KEYSTORE
    unset RELEASE_STORE_PASSWORD
    unset RELEASE_KEY_PASSWORD
    unset RELEASE_KEY_ALIAS
    unset GOOGLE_APPLICATION_CREDENTIALS
    unset KEYSTORE_PASSWORD
}

trap cleanup EXIT

# -----------------------------------------------------------------------------
# Authenticate
# -----------------------------------------------------------------------------

echo "Getting Google Play access token..."

TOKEN="$(
    gcloud auth application-default print-access-token \
        --scopes="https://www.googleapis.com/auth/androidpublisher"
)"

if [[ -z "$TOKEN" ]]; then
    echo "ERROR: Could not obtain Google OAuth access token."
    exit 1
fi

API_BASE="https://androidpublisher.googleapis.com/androidpublisher/v3"
UPLOAD_BASE="https://androidpublisher.googleapis.com/upload/androidpublisher/v3"

play_request() {
    local response
    local status
    if response="$(curl --fail-with-body --silent --show-error "$@")"; then
        printf '%s' "$response"
    else
        status=$?
        printf '\nGoogle Play request failed:\n%s\n' "$response" >&2
        return "$status"
    fi
}

# -----------------------------------------------------------------------------
# Create edit
# -----------------------------------------------------------------------------

echo "Creating Google Play edit..."

EDIT_RESPONSE="$(
    play_request \
        -X POST \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{}' \
        "$API_BASE/applications/$PACKAGE_NAME/edits"
)"

EDIT_ID="$(printf '%s' "$EDIT_RESPONSE" | jq -r '.id // empty')"

if [[ -z "$EDIT_ID" ]]; then
    echo "ERROR: Google Play did not return an edit ID."
    echo "$EDIT_RESPONSE"
    exit 1
fi

echo "Edit created: $EDIT_ID"

# Read every available artifact and track, including testing and draft releases.
echo "Checking existing Google Play version codes..."
BUNDLES_RESPONSE="$(play_request -H "Authorization: Bearer $TOKEN" "$API_BASE/applications/$PACKAGE_NAME/edits/$EDIT_ID/bundles")"
APKS_RESPONSE="$(play_request -H "Authorization: Bearer $TOKEN" "$API_BASE/applications/$PACKAGE_NAME/edits/$EDIT_ID/apks")"
TRACKS_RESPONSE="$(play_request -H "Authorization: Bearer $TOKEN" "$API_BASE/applications/$PACKAGE_NAME/edits/$EDIT_ID/tracks")"

# Preserve the known historical maximum even if an old artifact is no longer listed.
HIGHEST_VERSION_CODE="$(
    printf '%s\n' "$BUNDLES_RESPONSE" "$APKS_RESPONSE" "$TRACKS_RESPONSE" |
        jq -ers '
            [1678468727,
             (.[0].bundles[]?.versionCode | tonumber),
             (.[1].apks[]?.versionCode | tonumber),
             (.[2].tracks[]?.releases[]?.versionCodes[]? | tonumber)]
            | if all(.[]; . >= 1 and . <= 2100000000 and . == floor)
              then max
              else error("Invalid Google Play version code")
              end'
)"
if (( HIGHEST_VERSION_CODE >= 2100000000 )); then
    echo "ERROR: Google Play version code limit has been reached."
    exit 1
fi
RELEASE_VERSION_CODE=$((HIGHEST_VERSION_CODE + 1))

echo
echo "----------------------------------------"
echo "Android release"
echo "----------------------------------------"
echo "Module:  :$ANDROID_MODULE"
echo "Package: $PACKAGE_NAME"
echo "Track:   $TRACK"
echo "Build:   $RELEASE_VERSION_CODE"
echo "----------------------------------------"
echo

# -----------------------------------------------------------------------------
# Build signed AAB
# -----------------------------------------------------------------------------

echo "Building signed release bundle..."
echo

./gradlew ":${ANDROID_MODULE}:bundleProdRelease" "-PreleaseVersionCode=$RELEASE_VERSION_CODE"

AAB_DIRECTORY="$MODULE_DIR/build/outputs/bundle/prodRelease"

if [[ ! -d "$AAB_DIRECTORY" ]]; then
    echo
    echo "ERROR: Bundle output directory not found:"
    echo "  $AAB_DIRECTORY"
    exit 1
fi

AAB_PATH="$(
    find "$AAB_DIRECTORY" \
        -type f \
        -name "*.aab" \
        -print |
    head -n 1
)"

if [[ -z "$AAB_PATH" || ! -f "$AAB_PATH" ]]; then
    echo "ERROR: No release AAB found in:"
    echo "  $AAB_DIRECTORY"
    exit 1
fi

echo
echo "Built:"
echo "  $AAB_PATH"
echo

# -----------------------------------------------------------------------------
# Upload AAB
# -----------------------------------------------------------------------------

echo
echo "Uploading AAB..."

BUNDLE_RESPONSE="$(
    play_request \
        -X POST \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/octet-stream" \
        --data-binary "@$AAB_PATH" \
        "$UPLOAD_BASE/applications/$PACKAGE_NAME/edits/$EDIT_ID/bundles?uploadType=media"
)"

VERSION_CODE="$(printf '%s' "$BUNDLE_RESPONSE" | jq -r '.versionCode // empty')"

if [[ -z "$VERSION_CODE" ]]; then
    echo "ERROR: Google Play did not return a versionCode."
    echo "$BUNDLE_RESPONSE"
    exit 1
fi

echo "Uploaded versionCode: $VERSION_CODE"

# -----------------------------------------------------------------------------
# Assign bundle to track
# -----------------------------------------------------------------------------

echo
echo "Assigning versionCode $VERSION_CODE to '$TRACK'..."

TRACK_BODY="$(
    jq -n \
        --arg track "$TRACK" \
        --arg versionCode "$VERSION_CODE" \
        '{
            track: $track,
            releases: [
                {
                    versionCodes: [$versionCode],
                    status: "completed"
                }
            ]
        }'
)"

play_request \
    -X PUT \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$TRACK_BODY" \
    "$API_BASE/applications/$PACKAGE_NAME/edits/$EDIT_ID/tracks/$TRACK" \
    >/dev/null

# -----------------------------------------------------------------------------
# Commit
# -----------------------------------------------------------------------------

echo "Committing Google Play edit..."

play_request \
    -X POST \
    -H "Authorization: Bearer $TOKEN" \
    "$API_BASE/applications/$PACKAGE_NAME/edits/$EDIT_ID:commit" \
    >/dev/null

echo
echo "========================================"
echo "Release uploaded successfully"
echo "========================================"
echo "Package:      $PACKAGE_NAME"
echo "Version code: $VERSION_CODE"
echo "Track:        $TRACK"
echo "Bundle:       $AAB_PATH"
echo "========================================"
