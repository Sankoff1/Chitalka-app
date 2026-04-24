---
moduli_section: "§3.4"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#34-спецификации-экранов-библиотеки-контракт-uiданных"
tags: [screen-spec, BooksAndDocs, ReaderScreenSpec, BookListSearchFilter]
---

# §3.4. Модуль `library-kotlin` — спецификации экранов

Оглавление: [MODULI §3.4](../MODULI-I-KOMPONENTY.md#34-спецификации-экранов-библиотеки-контракт-uiданных)

Контракты для Compose в `app` (`ChitalkaDrawerRouter`, читалка). Unit-тесты: `chitalka-kotlin/library-kotlin/src/test/kotlin/com/chitalka/screens/.../*Test.kt`.

| Экран | Путь |
|-------|------|
| Сейчас читаю | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/readingnow/ReadingNowScreenSpec.kt` |
| Книги и документы | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/books/BooksAndDocsScreenSpec.kt` |
| Избранное | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/favorites/FavoritesScreenSpec.kt` |
| Корзина | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/trash/TrashScreenSpec.kt` |
| Настройки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/settings/SettingsScreenSpec.kt` |
| Отладочные логи | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/debuglogs/DebugLogsScreenSpec.kt` |
| Читалка | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/reader/ReaderScreenSpec.kt` |
| Раскладка списка | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/common/BookListScreenLayout.kt` |
| Поиск/фильтр | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/common/BookListSearchFilter.kt` |

Экран **отладочных логов**: `DebugLogsScreenSpec` задаёт подписи панели (очистить, **скопировать** — ключ `debugLogs.copy` в JSON каталоге, экспорт и т.д.); UI панели в модуле `app` — `ChitalkaDebugLogsPane`.

Связи: типы из [§3.1](sec-03-1-library-kotlin-tipy.md); строки/i18n из [§3.7](sec-03-7-library-kotlin-i18n-tema.md).
