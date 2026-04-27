---
id: lib-android-testy
tags: [androidTest]
module: library-android
path: library-android/src/androidTest/
---

# `library-android` — инструментальные тесты

## Расположение

- `chitalka-kotlin/library-android/src/androidTest/java/com/chitalka/storage/StorageServiceInstrumentedTest.kt`
- `chitalka-kotlin/library-android/src/androidTest/java/com/ncorti/kotlin/template/library/android/ToastUtilTest.kt`

## Назначение

- Проверка **реальной SQLite** и файловой системы на устройстве (`StorageService`).
- Тест шаблонного Toast (низкий приоритет для продукта Chitalka).

## Связи

| Тест | Покрываемый подмодуль |
|------|------------------------|
| `StorageServiceInstrumentedTest` | [lib-android-storage.md](lib-android-storage.md) |

Запуск через Gradle `connectedAndroidTest` / задачи модуля (зависит от конфигурации в [lib-android-koren.md](lib-android-koren.md)).
