# 2ools

[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-brightgreen.svg)](https://developer.android.com/jetpack/compose)

**2ools** is a small collection of useful Android utilities built to work offline.

The idea is simple: instead of installing a separate app for converting images, working with PDFs, generating QR codes, calculating EMI, checking EXIF data, and other small tasks, 2ools keeps them together in one app.

There are no ads, no tracking, and the app does not require internet access.

## What's included

### Documents & PDF

- **Images to PDF** — combine multiple images into an A4 PDF, reorder pages, rotate images, or apply a black-and-white filter.
- **PDF to Images** — extract PDF pages as PNG or JPG files.

### Image tools

- **Image Compressor** — reduce image size with a preview of the expected output.
- **Image Resizer** — resize using a percentage or exact pixel dimensions.
- **Format Converter** — convert between JPG, PNG, and WebP.
- **EXIF Inspector** — view image metadata such as camera information and GPS tags, with an option to remove metadata.

### QR & Barcode

- **QR Scanner** — scan codes using the camera or an image from the gallery.
- **QR Generator** — create QR codes for text, URLs, and Wi-Fi details.

### Calculators

- **Percentage Calculator** — percentage, ratio, increase/decrease, and relative-change calculations.
- **Date & Age Calculator** — calculate exact age and time remaining until the next birthday.
- **EMI Calculator** — calculate monthly loan repayments and view the interest/principal split.

### Unit conversion

Supports offline conversion for:

- Length
- Mass
- Area
- Volume
- Temperature

### Text & Security

- **Text Inspector** — count words, characters, syllables, and estimate reading time.
- **Case Converter** — convert text to uppercase, lowercase, title case, camelCase, snake_case, and kebab-case.
- **Hash Generator** — generate MD5, SHA-1, SHA-256, and SHA-512 hashes.
- **Password Vault** — generate passwords and store credentials locally in encrypted storage.

## Privacy

2ools is designed to work locally.

It does not contain:

- Ads
- Analytics
- Tracking
- Network permissions

Files are processed on the device instead of being uploaded to an external service.

## Tech stack

The app is written in **Kotlin** and uses **Jetpack Compose** for the UI.

Some of the main components used in the project are:

- Jetpack Compose + Material 3
- MVVM
- Koin
- Jetpack DataStore
- EncryptedSharedPreferences
- CameraX
- Google ML Kit Barcode Scanning
- ZXing
- Coil 3

The app uses a single-activity setup with Compose navigation.

## Project structure

The project is roughly organised like this:

```text
core/
├── designsystem/
├── registry/
└── storage/

features/
└── ...

ui/
└── navigation/
```

`core/designsystem` contains the common theme, typography, colours, and shared UI components.

`core/registry` contains `ToolRegistry`, which keeps track of the available tools, their categories, and navigation routes.

`core/storage` handles common file operations, temporary files, bitmap decoding, and scoped-storage related code.

Each tool lives under `features/` with its own Compose screen and related logic.

The main navigation graph is located in `ui/navigation/AppNavigation.kt`.

## Building

### Requirements

- Android Studio Koala / Ladybug or newer
- JDK 17
- Android SDK 35
- Minimum supported Android version: API 26

Clone the repository:

```bash
git clone https://github.com/NandanaMD/2ools-Android.git
cd 2ools-Android
```

Build the debug APK:

```bash
./gradlew assembleDebug
```

The APK will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

You can also open the project directly in Android Studio and run the `app` configuration on an emulator or physical device.

## Contributing

Bug reports, suggestions, and pull requests are welcome.

If you are adding a new utility, try to keep it consistent with the rest of the app:

- Prefer offline processing where possible.
- Avoid unnecessary permissions.
- Keep dependencies reasonably small.
- Follow the existing Compose and Material 3 UI style.

## License

2ools is licensed under the **GNU General Public License v3.0**.

You are free to use, modify, and redistribute the source code under the terms of the GPLv3.

See [LICENSE](LICENSE) for the full license text.