# Sonix AI 1.00 — Android AI Project Builder

**An AI-powered Android development assistant for creating, compiling, and managing software projects.**

Sonix AI 1.00 is an Android application starter project designed to help users create coding and modding projects through an AI assistant and integrated build tools.

Sonix connects to an OpenAI-compatible AI inference endpoint. The name `Sonix 1.00` is the application model alias; the APK does not contain a pretrained AI model.

## Development Configuration (Example)

| Component | Example Configuration |
|---|---|
| Google Android NDK | r23c and r30 LTS |
| Target ABI | armeabi-v7a (32-bit ARM) |
| Minimum Plugin Android API | 21 |
| C++ Standard | C++17 |
| Local Toolchain | Termux Clang and LLD |
| Remote Build Toolchain | Google Android NDK |
| Build Environment | Termux and GitHub Actions |
| Main Workspace | External microSD Card |
| Android Build System | Gradle |
| Programming Languages | Java and C++ |

Google NDK r23c and r30 are installed side by side in the GitHub build workflow. They are separate toolchains, not a single combined compiler.

NDK r23c is included for compatibility with older projects. NDK r30 is the newer LTS toolchain. Select the version that matches your project dependencies.

## Application Features

- AI Chat — Connect to an external AI inference endpoint.
- GTA San Andreas AML — Prepare Android AML C++ plugin projects.
- Minecraft Fabric — Prepare Fabric mod projects.
- Minecraft Forge — Prepare Forge and NeoForge mod projects.
- HTML5 Game Builder — Create HTML, CSS, and JavaScript projects.
- Android APK Builder — Build Android Gradle projects.
- Texture Editor — Manage project texture assets.
- C++ Compiler — Compile supported C++ projects.
- Mod Installer — Organize supported mod files.
- Project ZIP Exporter — Export projects as ZIP archives.
- AI Code Repair — Request code corrections using AI.
- Terminal SN — Execute approved build commands through Termux.
- NDK Center — Configure development tools and project storage.

Some features in this starter version are project-generation templates rather than complete visual editors or one-tap installers.

## Project Creation Workflow

1. Describe the application, game, or mod you want to create.
2. Select the appropriate project type.
3. Let the AI prepare the project files.
4. Save generated files to the selected workspace.
5. Review the proposed build command.
6. Approve the command before execution.
7. Compile the project using Termux or GitHub Actions.
8. Find the generated files in the build directory.

The AI Builder is designed to generate project files instead of displaying large amounts of source code in the chat.

## 1. External microSD Card Setup

1. Insert an external microSD card into your Android device.
2. Open your file manager.
3. Create a folder named `SonixNDK` on the external microSD card.
4. Install Sonix AI.
5. Open Sonix AI and navigate to NDK Center.
6. Select the `SonixNDK` folder on the external microSD card.
7. Grant the requested storage permissions.

The workspace structure:

```text
SonixNDK/
├── projects/
├── builds/
├── assets/
└── cache/
```

The actual storage path depends on the device. For example:

```text
/storage/ABCD-1234/SonixNDK/
```

On Android 11, some devices restrict access to the root of external storage. Select the `SonixNDK` subfolder instead.

## 2. Termux Installation and Configuration

Install Termux from an official source:

https://github.com/termux/termux-app

Open Termux and execute:

```bash
pkg update -y
pkg upgrade -y
termux-setup-storage
```

Install the development packages:

```bash
pkg install -y git gh clang lld make cmake ninja pkg-config python ndk-sysroot ndk-multilib openjdk-17 gradle wget curl unzip zip file nano
```

Enable external application commands:

```bash
mkdir -p ~/.termux
printf '\nallow-external-apps=true\n' >> ~/.termux/termux.properties
termux-reload-settings
```

Restart Termux if necessary.

Sonix uses the Termux `RUN_COMMAND` interface to request command execution. Android permissions and Termux configuration must be enabled before this feature can work.

Termux build files are staged in private Termux storage because public shared storage may not support all compiler operations, symbolic links, and executable permissions.

The external microSD card remains the main project workspace.

## 3. AI Endpoint Configuration

Sonix requires an AI inference endpoint compatible with:

```text
POST /v1/chat/completions
```

Configure the following in Sonix Settings:

- HTTPS API endpoint
- Model identifier recognized by the server
- API key, if required

Save the configuration before using AI Chat.

The application does not currently include an offline pretrained AI model.

## 4. Creating GTA San Andreas AML Plugins

Target configuration:

```text
Game: GTA San Andreas Android
Loader: Android Mod Loader (AML)
ABI: armeabi-v7a
Language: C++17
Minimum Android API: 21
Output: Shared library (.so)
```

The AI Builder can prepare an AML project based on the requested mod description.

Example request:

```text
Target: GTA San Andreas Android 32-bit.
Loader: Android Mod Loader.
Create a C++17 plugin.
Target ABI: armeabi-v7a.

Generate the required project files.
Use compatible AML APIs and headers.
Explain the plugin functionality in the project summary.
Do not modify unrelated gameplay features.
```

### Local Termux Build

Sonix attempts to use the local Termux build script.

Termux Clang, `ndk-sysroot`, and `ndk-multilib` may not provide every component required by all ARM32 Android projects.

If the ARM32 linker or required runtime is unavailable, use the Google NDK workflow on GitHub.

### Google NDK Build Through GitHub

1. Generate an AML project using Sonix Builder.
2. Export the project as a ZIP archive.
3. Upload the ZIP to the root of your GitHub repository.
4. Open the repository's Actions tab.
5. Select **Build GTA SA AML Plugin (.so)**.
6. Select **Run workflow**.
7. Enter the project ZIP filename.
8. Select the desired NDK version or both versions.
9. Wait for the build jobs to finish.
10. Download the generated artifacts.

Expected artifact names:

```text
Sonix-AML-r23c-armeabi-v7a
Sonix-AML-r30-armeabi-v7a
```

Choose the plugin compatible with the target game, AML version, and dependencies.

A successful compilation does not automatically guarantee compatibility with every GTA San Andreas Android version.

## 5. Other Project Types

### Minecraft Fabric and Forge

Sonix can prepare Java and Gradle mod project templates.

The required Java version depends on the Minecraft version and mod loader.

### HTML5 Game Builder

Create projects using:

- HTML
- CSS
- JavaScript

Projects can be exported as ZIP archives.

### Android APK Builder

Android applications use Gradle.

GitHub Actions provides an Android SDK build environment for building APK projects.

### C++ Compiler

Supported CMake and Make projects can be compiled using Termux or a compatible GitHub build environment.

### Texture Editor and Mod Installer

The starter project provides AI-assisted project templates and file organization workflows. Full graphical texture editing and one-tap mod installation are not yet implemented.

### AI Code Repair

Provide source code and compiler error messages to the AI Builder so it can generate corrected project files.

Automatic repair of imported legacy project ZIP archives is not yet implemented.

## 6. Building Sonix AI APK

1. Create a GitHub repository.
2. Upload the Sonix AI project source.
3. Make sure the hidden `.github/workflows` directory is included.
4. Open the repository's Actions tab.
5. Select **Build Sonix AI APK**.
6. Select **Run workflow**.
7. Wait for the build to finish.
8. Download the `SonixAI-debug-apk` artifact.
9. Extract the downloaded ZIP archive.
10. Install `app-debug.apk` on your Android device.

The Sonix Android application is a Java-based Android project. Google NDK is used by the separate native plugin build workflow.

## 7. Building AI-Generated Projects

The GitHub workflow can build supported generated projects.

1. Export the generated project as a ZIP archive.
2. Upload the ZIP to the repository root.
3. Open Actions.
4. Select **Build Sonix Generated Project**.
5. Select **Run workflow**.
6. Enter the project ZIP filename.
7. Select the appropriate build mode.
8. Download the generated artifact.

Supported build modes depend on the project:

- `android_apk`
- `gradle`
- `auto`
- `zip_only`

The project must contain the necessary Gradle files, dependencies, and configuration.

## 8. AI Model Training with GitHub Actions

Sonix includes an experimental LoRA fine-tuning workflow.

Default base model:

```text
Qwen/Qwen2.5-Coder-0.5B-Instruct
```

Training dataset:

```text
training/dataset.jsonl
```

Training workflow:

```text
Train Sonix LoRA (CPU on GitHub)
```

To run training:

1. Add high-quality coding examples to `training/dataset.jsonl`.
2. Commit the dataset to GitHub.
3. Open Actions.
4. Select **Train Sonix LoRA (CPU on GitHub)**.
5. Select **Run workflow**.
6. Configure the training steps.
7. Wait for training to finish.
8. Download the `Sonix-LoRA-Adapter` artifact.

The workflow has a maximum configured duration of four hours. Training may finish earlier.

Important:

- GitHub-hosted standard runners use CPU resources unless a suitable GPU runner is separately available.
- LoRA creates an adapter; it does not train a large AI model from scratch.
- Training quality depends on dataset size and quality.
- The trained adapter requires a compatible inference server.
- GitHub Actions artifacts are not a permanent AI hosting service.

To serve the trained adapter, configure the inference server in the `server/` directory and connect its HTTPS endpoint to Sonix Settings.

## 9. Troubleshooting

### Library file not found

Use the direct ZIP download link provided with the project. If the ChatGPT application cannot download the file, try opening the same conversation in a browser.

### External microSD Card Cannot Be Selected

Create a `SonixNDK` subfolder on the external microSD card and select that folder instead of the card root.

### Storage Permission Denied

Check Android storage permissions for Sonix and Termux.

### Termux Command Execution Failed

Check:

- `allow-external-apps=true`
- Termux external command permissions
- Android `RUN_COMMAND` permission
- Termux installation source compatibility

### ndk-build Not Found

Use the Google NDK AML workflow on GitHub.

### ARM32 Linker Errors

Check the ABI, Android API level, C++ runtime, AML headers, and native dependencies.

### AI Does Not Respond

Verify the HTTPS endpoint, model identifier, internet connection, and API key.

## Technical References

- Android NDK: https://developer.android.com/ndk/downloads
- Android Mod Loader: https://github.com/RusJJ/AndroidModLoader
- Termux: https://github.com/termux/termux-app
- Termux RUN_COMMAND: https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent
- GitHub Actions: https://docs.github.com/en/actions
- Hugging Face PEFT: https://huggingface.co/docs/transformers/peft

## License

Add the appropriate license for this project before distributing it.

---

**Sonix AI 1.00 — Android AI Project Builder**
