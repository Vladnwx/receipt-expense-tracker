# QR Code Scanner

Modern Android application for scanning QR codes using CameraX and ML Kit.

## Features

- 📷 Real-time QR code scanning using CameraX
- 🎯 ML Kit barcode scanning for accurate detection
- 🔒 Runtime permission handling
- 🎨 Material Design 3 UI
- 🌙 Day/Night theme support
- 📦 Automated GitHub Releases

## Tech Stack

- **Language**: Kotlin
- **UI**: Material Design 3, ViewBinding
- **Camera**: CameraX
- **QR Scanning**: Google ML Kit Barcode Scanning
- **Build System**: Gradle with Kotlin DSL
- **CI/CD**: GitHub Actions

## Requirements

- Android SDK 24+ (Android 7.0 Nougat)
- Target SDK 34 (Android 14)
- Java 17
- Camera hardware

## Permissions

The app requires the following permissions:
- `CAMERA` - For QR code scanning

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Releasing

To create a new release:

1. Update version in `app/build.gradle.kts`
2. Create and push a tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Actions will automatically build and create a release

## Project Structure

```
app/
├── src/main/
│   ├── java/com/qrcode/scanner/
│   │   └── MainActivity.kt
│   ├── res/
│   │   ├── layout/
│   │   ├── values/
│   │   ├── drawable/
│   │   └── mipmap-*/
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## License

MIT License
