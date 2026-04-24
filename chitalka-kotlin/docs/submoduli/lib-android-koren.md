---
id: lib-android-koren
tags: [android-library, gradle, proguard-consumer]
module: library-android
---

# Модуль `library-android/` — корень

## Расположение

- `chitalka-kotlin/library-android/build.gradle.kts`
- `chitalka-kotlin/library-android/consumer-rules.pro`
- `chitalka-kotlin/library-android/proguard-rules.pro`

## Назначение

Android-библиотека с **SQLite**, **EPUB**, **системным picker**, **координатором навигации**, **SharedPreferences**-обёрткой. Публикуется как AAR (см. блок `publishing` в Gradle при включении).

## Зависимости Gradle

- `implementation(project(":library-kotlin"))` — все доменные типы и контракты.

## Связи

- Потребляется **`app`**: все подмодули `com.chitalka.*` ниже.
- Документация по пакетам: [lib-android-storage.md](lib-android-storage.md) … [lib-android-testy.md](lib-android-testy.md).
