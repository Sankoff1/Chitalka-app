---
name: chitalka-modules
description: >-
  Gradle modules, layer boundaries, and file map for chitalka-kotlin (Android
  app, library-kotlin, library-android). Use when planning changes, adding
  features, or deciding where new code belongs.
---

# Chitalka Kotlin — модули и границы

Рабочий корень: каталог `chitalka-kotlin/` (пути ниже относительно него).

## Модули

| Модуль | Назначение |
|--------|------------|
| **app** | Android-приложение: Compose UI, навигация, интеграция читалки (WebView). |
| **library-kotlin** | Общая JVM/Kotlin-логика **без Android**: модели, спецификации экранов, навигация, мост к Web-читалке, unit-тесты. |
| **library-android** | Платформа Android: SQLite, EPUB, выбор файла, координатор навигации в читалку, SharedPreferences. |
| **library-compose** | Шаблон (демо factorial); **не подключён** к `app` — не использовать для основного UI. |

## Правила размещения кода

- Доменные типы, сессия библиотеки, спецификации экранов (`*ScreenSpec`), мост читалки (`ReaderBridge*`) — только **library-kotlin**.
- Доступ к БД, файловой системе EPUB, `ActivityResult` для выбора файла — только **library-android**; из `app` — через публичный API модулей.
- Compose-экраны, `NavHost`, тема, `WebView` в UI — **app**.

## Каноническая карта

Полная таблица файлов и типовых задач: `docs/MODULI-I-KOMPONENTY.md` и `docs/modules/README.md`.

## Типовые задачи → файлы

| Задача | Где искать |
|--------|------------|
| Корневой граф / стек / читалка | `app/src/main/java/.../ui/ChitalkaNavHost.kt`, `ChitalkaNavigationSetup.kt` |
| Drawer и разделы | `ChitalkaMainShell.kt`, `ChitalkaDrawerRouter.kt`; спецификация drawer в `library-kotlin/.../DrawerNavigationSpec.kt` |
| Маршруты Reader | `app/.../AppNavRoutes.kt`; типы в `library-kotlin/.../navigation/` |
| JS-мост читалки | `library-kotlin/.../readerview/ReaderBridgeScripts.kt`, `ReaderBridgeMessages.kt` |
| Импорт EPUB | `library-android/.../epub/`, `library/ImportEpubToLibrary.kt` |
| Библиотека в SQLite | `library-android/.../storage/StorageService.kt` |

## Сборка

- Kotlin JVM target и Android: **17** (`app/build.gradle.kts`).
- Compose + Material3; линт: `warningsAsErrors`, `abortOnError` — новые предупреждения ломают сборку.

When in doubt, open `docs/MODULI-I-KOMPONENTY.md` section matching the feature area before editing.
