#!/usr/bin/env bash

set -euo pipefail

KEYSTORE_PATH="${1:-}"
PLAY_CREDENTIALS_PATH="${2:-}"
TRACK="${3:-internal}"
KEY_ALIAS="${KEY_ALIAS:-upload}"

if [[ -z "$KEYSTORE_PATH" || -z "$PLAY_CREDENTIALS_PATH" ]]; then
    echo "Usage:"
    echo "  $0 <keystore.jks> <play-api.json> [track]"
    echo
    echo "Example:"
    echo "  $0 ~/AndroidKeys/upload-keystore.jks ~/AndroidKeys/play-api.json internal"
    exit 1
fi

if [[ ! -f "$KEYSTORE_PATH" ]]; then
    echo "Keystore not found: $KEYSTORE_PATH"
    exit 1
fi

if [[ ! -f "$PLAY_CREDENTIALS_PATH" ]]; then
    echo "Play credentials not found: $PLAY_CREDENTIALS_PATH"
    exit 1
fi

read -r -s -p "Upload keystore password: " KEYSTORE_PASSWORD
echo

cleanup() {
    unset RELEASE_STORE_PASSWORD
    unset RELEASE_KEY_PASSWORD
    unset RELEASE_KEYSTORE
    unset RELEASE_KEY_ALIAS
    unset ANDROID_PUBLISHER_CREDENTIALS
}

trap cleanup EXIT

export RELEASE_KEYSTORE="$(cd "$(dirname "$KEYSTORE_PATH")" && pwd)/$(basename "$KEYSTORE_PATH")"
export RELEASE_STORE_PASSWORD="$KEYSTORE_PASSWORD"
export RELEASE_KEY_PASSWORD="$KEYSTORE_PASSWORD"
export RELEASE_KEY_ALIAS="$KEY_ALIAS"

# Gradle Play Publisher reads this directly.
export ANDROID_PUBLISHER_CREDENTIALS
ANDROID_PUBLISHER_CREDENTIALS="$(cat "$PLAY_CREDENTIALS_PATH")"

echo
echo "Release:"
echo "  Keystore: $RELEASE_KEYSTORE"
echo "  Alias:    $RELEASE_KEY_ALIAS"
echo "  Track:    $TRACK"
echo

echo "Running tests..."
./gradlew test

echo
echo "Building and uploading release bundle..."
./gradlew publishBundle --track="$TRACK"

echo
echo "Done."
echo "Uploaded to Google Play track: $TRACK"s