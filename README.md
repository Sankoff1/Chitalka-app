# Chitalka (Android)

## Быстрый старт (демо на Android)
1. Подключи телефон (USB debugging) или запусти эмулятор.
2. В корне проекта выполни:

```bash
./gradlew :app:installDebug
```

Приложение соберется и установится на устройство.

## Собрать APK
Debug APK:

```bash
./gradlew :app:assembleDebug
```

Release APK:

```bash
./gradlew :app:assembleRelease
```

Готовые APK: `app/build/outputs/apk/`
