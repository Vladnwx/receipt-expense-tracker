# ReceiptExpenseTracker

Android-приложение для сканирования QR-кодов чеков (ФНС), их разбора и учёта расходов.

## Возможности

- 📷 Сканирование QR-кодов чеков через CameraX + ML Kit
- 📄 Сохранение сырых данных чека в Room
- ☁️ Автоматический запрос состава чека через API ФНС
- 🏷️ Разбор товаров и привязка к категориям
- 💰 Учёт расходов с фильтром по дате и категориям
- 🎨 Интерфейс Material Design 3
- 📦 Автоматические релизы через GitHub

## Технологический стек

- **Язык**: Kotlin
- **UI**: Material Design 3, ViewBinding
- **Камера**: CameraX
- **Сканирование QR**: Google ML Kit Barcode Scanning
- **Система сборки**: Gradle с Kotlin DSL
- **CI/CD**: GitHub Actions

## Требования

- Android SDK 24+ (Android 7.0 Nougat)
- Target SDK 34 (Android 14)
- Java 17
- Наличие камеры

## Разрешения

Приложение требует следующие разрешения:
- `CAMERA` — для сканирования QR-кодов

## Сборка

```bash
# Debug-версия
./gradlew assembleDebug

# Release-версия
./gradlew assembleRelease
```

## Публикация релиза

Для создания нового релиза:

1. Обновите версию в `app/build.gradle.kts`
2. Создайте и отправьте тег:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Actions автоматически соберёт и создаст релиз

## Структура проекта

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

## Лицензия

Лицензия MIT
