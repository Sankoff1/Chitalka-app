---
id: app-koren
tags: [android-app, gradle, proguard]
module: app
---

# Модуль `app/` — корень (конфигурация)

## Расположение

- `chitalka-kotlin/app/build.gradle.kts`
- `chitalka-kotlin/app/proguard-rules.pro`

## Назначение

- Объявить модуль как **Android Application** (`com.android.application`), плагины Kotlin и Compose.
- Задать `applicationId`, `namespace`, `minSdk`/`compileSdk`/`targetSdk`, флаги Compose, зависимости.
- Правила **ProGuard/R8** для release (сейчас часто с отключённым minify в debug-конфигурациях — смотреть актуальный `build.gradle.kts`).

## Зависимости (Gradle)

- `implementation(projects.libraryAndroid)`
- `implementation(projects.libraryKotlin)`
- **Нет** `library-compose`.

## Связи

- Исходники приложения: [app-vhod.md](app-vhod.md), [app-ui-yadro.md](app-ui-yadro.md) и остальные `app-ui-*.md`, [app-resursy.md](app-resursy.md), [app-testy.md](app-testy.md).
- Версии и `APP_ID` часто тянутся из **`gradle.properties`** корня: [proekt-koren.md](proekt-koren.md).
