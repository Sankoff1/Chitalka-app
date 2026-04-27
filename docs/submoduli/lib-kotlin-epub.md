---
id: lib-kotlin-epub
tags: [EpubErrorCodes]
module: library-kotlin
package: com.chitalka.epub
---

# `library-kotlin` — подмодуль `com.chitalka.epub`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/epub/EpubErrorCodes.kt`

## Назначение

Общие **коды/ключи ошибок EPUB**, понятные и JVM-, и Android-слою, и UI (строки в каталоге i18n).

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [lib-android-epub.md](lib-android-epub.md) | `EpubService` и импорт отображают/пробрасывают ошибки |
| [app-ui-reader.md](app-ui-reader.md) | Ошибки открытия книги в читалке |

Разбор ZIP/OPF живёт только в Android-модуле: [lib-android-epub.md](lib-android-epub.md). Сообщения `EpubServiceError` с произвольным текстом (разбор OCF) до UI доходят без подмены; стабильные коды из этого файла мапятся на i18n в `ReaderScreenSpec` — см. [§3.9](../moduli-detail/sec-03-9-library-kotlin-epub-oshibki.md).
