#!/bin/bash
# Setup keystore for local release builds
# NOTE: Passwords are NOT hardcoded — read from env vars or prompted interactively.

KEYSTORE_DIR="app"
KEYSTORE_FILE="$KEYSTORE_DIR/calcduo-keystore.jks"
KEY_ALIAS="calcduo"

echo "🔑 Generating keystore for release builds..."

# Read passwords securely
KEY_PASSWORD="${KEY_PASSWORD:-}"
STORE_PASSWORD="${STORE_PASSWORD:-}"

if [ -z "$KEY_PASSWORD" ] || [ -z "$STORE_PASSWORD" ]; then
    read -rsp "Enter keystore password: " STORE_PASSWORD
    echo ""
    read -rsp "Enter key password (press Enter to use keystore password): " KEY_PASSWORD
    echo ""
    if [ -z "$KEY_PASSWORD" ]; then
        KEY_PASSWORD="$STORE_PASSWORD"
    fi
fi

if ! command -v keytool &> /dev/null; then
    if [ -n "$JAVA_HOME" ]; then
        KEYTOOL="$JAVA_HOME/bin/keytool"
    else
        echo "❌ JDK not found. Install JDK 17+ first."
        exit 1
    fi
else
    KEYTOOL="keytool"
fi

keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$STORE_PASSWORD" -keypass "$KEY_PASSWORD" \
    -dname "CN=CalcDuo, OU=Developer, O=soe1hom-arch, C=ID" -noprompt

echo "✅ Keystore created: $KEYSTORE_FILE"
echo ""
echo "📋 To configure GitHub Secrets for CI/CD, refer to:"
echo "   https://github.com/soe1hom-arch/calcduo/settings/secrets/actions"
