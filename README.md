# Khatty

Khatty (خطّي) is an offline-first Android app for writing Arabic and English text onto photos and designs. It lets users pick an image, add text layers, adjust layout, save projects locally, and export finished designs as PNG or JPEG without requiring internet access.

## English summary

Khatty is a lightweight Android editor for photo-based text design. It supports Arabic RTL, English LTR, local project storage, and export to real image files. The app is designed for offline use and can be extended into a full commercial product.

## Project status

This repository currently includes the Android project foundation and the primary editing workflow: choose image, add Arabic/English text, move text, save project JSON, and export a final image. Additional design and template features are scaffolded and can be extended further.

## Features implemented

- Android Java + XML project structure
- RTL support with Arabic and English text handling
- Image picker using Android document access API
- Text layers with editing, movement, color, size, rotation, and opacity support
- Local project saving and project reload
- Export to PNG/JPEG to the Pictures/Khatty folder
- GitHub Actions workflow to build the APK
- Offline-first behavior with no internet dependency

## Repository information

- App package: `com.khatty.editor`
- App name: `Khatty`
- Arabic name: `خطّي`
- Default package owner: `piko33300-dot`
- Repository: `Khatty-Android100`

## How to build

1. Open the project in Android Studio.
2. Install JDK 17 and Android SDK.
3. Sync Gradle.
4. Run `app:assembleDebug` or use Android Studio Build > Build Bundle(s) / APK(s).
5. The output APK is created under `app/build/outputs/apk/debug/`.

## GitHub Actions APK

The workflow in `.github/workflows/android-build.yml` builds the project, runs available tests, and uploads the APK as a GitHub Actions artifact.

## Local app data

Projects and generated output are stored locally on-device. The app uses internal app storage for project JSON files and shared storage for exported images under `Pictures/Khatty`.

## Notes

This project is created as a real Android app foundation with offline editing as the primary path. Some advanced features (full sticker libraries, font packs, and premium UI polish) are designed for extension without requiring internet access.
