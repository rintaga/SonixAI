#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
NAME="$(basename "$PROJECT_DIR")"
PRIVATE_OUT="$HOME/.sonix/builds/$NAME"
SD_ROOT="${SONIX_SDK_ROOT:-$HOME/.sonix}"
mkdir -p "$PRIVATE_OUT"
(cd "$(dirname "$PROJECT_DIR")" && zip -qr "$PRIVATE_OUT/$NAME-source.zip" "$NAME" -x '*/build/*')
if mkdir -p "$SD_ROOT/builds/$NAME" 2>/dev/null && cp -v "$PRIVATE_OUT/$NAME-source.zip" "$SD_ROOT/builds/$NAME/" 2>/dev/null; then
  echo "ZIP siap di microSD: $SD_ROOT/builds/$NAME/$NAME-source.zip"
else
  echo "ZIP siap di storage privat Termux: $PRIVATE_OUT/$NAME-source.zip"
fi
