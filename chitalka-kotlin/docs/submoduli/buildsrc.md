---
id: buildsrc
tags: [gradle, build-logic, publish, cleanup]
sostav_section: "buildSrc/"
---

# Подмодуль `buildSrc/`

## Расположение

`chitalka-kotlin/buildSrc/` — специальный подпроект Gradle, подключаемый **до** конфигурации остальных модулей.

## Назначение

Хранить **общую логику сборки** в виде precompiled script plugins или convention plugins: публикация артефактов, очистка, переиспользуемые настройки. Содержимое **не попадает в APK** и не является runtime-зависимостью приложения.

## Состав

| Файл | Роль |
|------|------|
| `buildSrc/build.gradle.kts` | Зависимости и плагины самого `buildSrc` |
| `buildSrc/settings.gradle.kts` | Настройки включённых build-logic при необходимости |
| `src/main/kotlin/publish.gradle.kts` | Логика публикации (Maven и т.п.) для библиотечных модулей |
| `src/main/kotlin/cleanup.gradle.kts` | Задачи/cleanup для CI или локальной сборки |

## Связи

- Подключается из **`library-android`**, **`library-kotlin`** (см. `id("publish")` и др. в их `build.gradle.kts`).
- С корнем проекта: [proekt-koren.md](proekt-koren.md).
