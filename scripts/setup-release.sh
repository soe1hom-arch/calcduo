#!/bin/bash
# Setup keystore for local release builds

KEYSTORE_DIR="app"
KEYSTORE_FILE="$KEYSTORE_DIR/calcduo-keystore.jks"
KEY_ALIAS="calcduo"
KEY_PASSWORD=""
STORE_PASSWORD=""

echo "🔑 Generating keystore for release builds..."

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
echo "📋 GitHub Secrets needed for CI/CD:"
echo "  KEYSTORE_BASE64  = base64 of $KEYSTORE_FILE"
echo "  KEY_ALIAS         = $KEY_ALIAS"
