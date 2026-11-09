#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

printf '\n[Sonix] Update Termux packages...\n'
pkg update -y
pkg upgrade -y

printf '\n[Sonix] Install C/C++ and Java tools...\n'
pkg install -y git gh clang lld make cmake ninja pkg-config python ndk-sysroot ndk-multilib openjdk-17 gradle wget curl unzip zip file

printf '\n[Sonix] Create private build staging folders...\n'
mkdir -p "$HOME/.sonix"/{cache,workspaces,builds}

printf '\n[Sonix] Configure RUN_COMMAND access...\n'
mkdir -p "$HOME/.termux"
if ! grep -q '^allow-external-apps=true$' "$HOME/.termux/termux.properties" 2>/dev/null; then
  printf '\nallow-external-apps=true\n' >> "$HOME/.termux/termux.properties"
fi
termux-reload-settings || true

printf '\n[Sonix] Done.\n'
echo "Private work area: $HOME/.sonix"
echo 'Choose the microSD folder SonixNDK inside Sonix AI > NDK Center.'
echo 'The source project stays on microSD; compilers build from a private copy inside Termux.'
echo 'Google NDK r23c/r30 builds are run by the GitHub Actions workflow.'
