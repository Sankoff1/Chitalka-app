---
name: chitalka-library-kotlin
description: >-
  Pure Kotlin library-kotlin layer: screen specs, library session, navigation
  types, reader Web bridge, i18n, theme preferences. Use when changing domain
  logic or UI contracts without Android APIs.
---

# Chitalka — library-kotlin (без Android)

Пакет: `com.chitalka.*` под `chitalka-kotlin/library-kotlin/src/main/kotlin/`.

## Назначение слоя

Здесь **нет** Android SDK: только модели, правила UI-экранов, навигация, мост к WebView-читалке, локализация и настройки темы в виде типов/логики.

## Ключевые области

### Данные и сессия библиотеки

- Типы: `com.chitalka.core.types` (книги, прогресс).
- Состояние сессии: `LibrarySessionState.kt`, поиск: `LibraryBookLookup.kt`.
- Последняя книга: `LastOpenBook.kt`, восстановление читалки: `LastOpenReaderRestore.kt`.

### Навигация

- `NavTypes.kt`, `RootStackDestination.kt`, `DrawerNavigationSpec.kt`, `ReaderRouteLifecycle.kt`.

### Спецификации экранов (`*ScreenSpec`)

Папка `com.chitalka.screens`: ReadingNow, BooksAndDocs, Favorites, Trash, Settings, DebugLogs, Reader и общие `screens/common/` (раскладка списка, поиск/фильтр).

При добавлении экрана: новый `*ScreenSpec` + unit-тесты в `library-kotlin/src/test/kotlin/` с тем же пакетом.

### UI-контракты (карточка, панели)

- `com.chitalka.ui.bookcard`, `bookactions`, `topbar`, `firstlaunch`.

### Мост читалки (скрипты и сообщения)

- `com.chitalka.ui.readerview`: `ReaderBridgeScripts.kt`, `ReaderBridgeMessages.kt`, `ReaderDarkModeHtml.kt`, `ReaderPageDirection.kt`.

Изменения здесь должны оставаться согласованы с `ChitalkaReaderWebView.kt` и полифиллами в **app**.

### i18n и тема

- `com.chitalka.i18n`: каталог строк, типы локали, предпочтения языка.
- `com.chitalka.theme`: палитра, предпочтения темы.

### EPUB (чистая логика)

- Результат выбора и URI: `com.chitalka.picker`, коды ошибок: `com.chitalka.epub/EpubErrorCodes.kt`.

### Отладка

- `com.chitalka.debug`: лог, консоль, автозагрузка EPUB (логика; Android-runner в library-android).

## Наследие шаблона

`com.ncorti.kotlin.template.library.FactorialCalculator` — демо шаблона; не смешивать с доменом Chitalka.
