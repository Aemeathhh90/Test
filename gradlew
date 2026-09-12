#!/bin/sh

# KakaAnime Gradle bootstrap wrapper.
# Downloads the pinned Gradle distribution when it is not available locally,
# then delegates all arguments to Gradle.

set -eu

GRADLE_VERSION="8.11.1"
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}/kakaanime-gradle/$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/gradle-$GRADLE_VERSION/bin/gradle"
DIST_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_BIN" ]; then
    TMP_DIR="${TMPDIR:-/tmp}/kakaanime-gradle"
    ZIP_FILE="$TMP_DIR/gradle-$GRADLE_VERSION-bin.zip"
    EXTRACT_DIR="$TMP_DIR/extracted-$GRADLE_VERSION"

    mkdir -p "$TMP_DIR" "$GRADLE_HOME"

    if [ ! -f "$ZIP_FILE" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl -fL --retry 3 --connect-timeout 20 -o "$ZIP_FILE" "$DIST_URL"
        elif command -v wget >/dev/null 2>&1; then
            wget -O "$ZIP_FILE" "$DIST_URL"
        else
            echo "Error: curl or wget is required to download Gradle." >&2
            exit 1
        fi
    fi

    rm -rf "$EXTRACT_DIR"
    mkdir -p "$EXTRACT_DIR"
    unzip -q -o "$ZIP_FILE" -d "$EXTRACT_DIR"
    rm -rf "$GRADLE_HOME/gradle-$GRADLE_VERSION"
    mv "$EXTRACT_DIR/gradle-$GRADLE_VERSION" "$GRADLE_HOME/"
fi

exec "$GRADLE_BIN" "$@"
