---
id: lib-compose
tags: [template, compose-material2, unused-in-app]
module: library-compose
---

# Модуль `library-compose/` (целиком)

## Расположение

- `chitalka-kotlin/library-compose/build.gradle.kts`, `consumer-rules.pro`, `proguard-rules.pro`, `.gitignore`
- `library-compose/src/main/java/com/ncorti/kotlin/template/app/ComposeActivity.kt`
- `library-compose/src/main/java/com/ncorti/kotlin/template/app/ui/components/Factorial.kt`
- `library-compose/src/main/AndroidManifest.xml`
- `library-compose/src/androidTest/.../FactorialTest.kt`

## Назначение

Отдельная **демо-библиотека** с Compose Material 2 и экраном факториала. Используется как артефакт шаблона; **`app` не зависит от этого модуля** — в основной APK Chitalka не входит.

## Зависимости Gradle

- `implementation(projects.libraryKotlin)` — только [lib-kotlin-shablon.md](lib-kotlin-shablon.md) `FactorialCalculator`.

## Связи

| Кто мог бы подключить | Статус |
|------------------------|--------|
| [app-koren.md](app-koren.md) | Зависимости нет — см. `app/build.gradle.kts` |

При подключении к другому приложению учитывать конфликт **Material2** (здесь) vs **Material3** в `app`.
