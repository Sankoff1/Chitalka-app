---
id: lib-kotlin-koren
tags: [jvm-library, gradle, kotlin-jvm]
module: library-kotlin
---

# Модуль `library-kotlin/` — корень

## Расположение

`chitalka-kotlin/library-kotlin/build.gradle.kts` — плагин `java-library`, Kotlin JVM, toolchain 17.

## Назначение

- Собрать **чистую JVM-библиотеку** без Android SDK: домен, навигационные типы, спеки экранов, мост читалки, i18n, утилиты.
- Подключить **kotlinx-serialization** и **coroutines** (см. зависимости в файле).

## Зависимости Gradle

- Не зависит от `app` / `library-android` / `library-compose`.
- Потребляется: **`app`**, **`library-android`**, **`library-compose`** (только шаблон).

## Связи

Подмодули пакетов: [lib-kotlin-core-types.md](lib-kotlin-core-types.md) … [lib-kotlin-testy.md](lib-kotlin-testy.md).
