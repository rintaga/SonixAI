#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
SOURCE="$(cd "$(dirname "$0")/.." && pwd)"
NAME="$(basename "$SOURCE")"
WORK="$HOME/.sonix/workspaces/$NAME"
PRIVATE_OUT="$HOME/.sonix/builds/$NAME"
SD_ROOT="${SONIX_SDK_ROOT:-$HOME/.sonix}"
mkdir -p "$WORK" "$PRIVATE_OUT"
rm -rf "$WORK"
cp -R "$SOURCE" "$WORK"
cd "$WORK"
if [ ! -f mod/amlmod.h ]; then
  command -v git >/dev/null 2>&1 || { echo "Install git: pkg install git"; exit 3; }
  TMP="$HOME/.cache/sonix-aml-sdk"
  if [ ! -d "$TMP/.git" ]; then rm -rf "$TMP"; git clone --depth 1 https://github.com/RusJJ/AndroidModLoader "$TMP"; fi
  test -f "$TMP/template_of_mod/mod/amlmod.h" || { echo "AML header template not found"; exit 4; }
  mkdir -p mod include external
  cp -R "$TMP/template_of_mod/mod/." mod/
  [ ! -d "$TMP/include" ] || cp -R "$TMP/include/." include/
  [ ! -d "$TMP/external" ] || cp -R "$TMP/external/." external/
fi
if [ -n "${ANDROID_NDK_HOME:-}" ] && [ -x "$ANDROID_NDK_HOME/ndk-build" ]; then
  NDK_BUILD="$ANDROID_NDK_HOME/ndk-build"
elif command -v ndk-build >/dev/null 2>&1; then
  NDK_BUILD="$(command -v ndk-build)"
else
  echo "No ndk-build found. Termux Clang is not necessarily a complete ARM32 NDK toolchain."
  echo "Use GitHub Actions -> Build GTA SA AML Plugin (.so), select both NDK versions."
  exit 10
fi
"$NDK_BUILD" NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=Android.mk NDK_APPLICATION_MK=Application.mk NDK_LIBS_OUT=build/libs
cp -v build/libs/armeabi-v7a/*.so "$PRIVATE_OUT"/
(cd "$(dirname "$WORK")" && zip -qr "$PRIVATE_OUT/$NAME-source.zip" "$NAME" -x '*/build/*')
if mkdir -p "$SD_ROOT/builds/$NAME" 2>/dev/null && cp -v "$PRIVATE_OUT"/* "$SD_ROOT/builds/$NAME"/ 2>/dev/null; then
  echo "BUILD OK. Files copied to microSD: $SD_ROOT/builds/$NAME"
else
  echo "BUILD OK. Termux cannot write to microSD, results remain at: $PRIVATE_OUT"
fi
