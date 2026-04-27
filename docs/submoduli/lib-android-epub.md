---
id: lib-android-epub
tags: [epub, zip, opf, metadata]
module: library-android
package: com.chitalka.epub
---

# `library-android` — подмодуль `com.chitalka.epub`

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/`

| Файл | Роль |
|------|------|
| `EpubIo.kt` | Низкоуровневое чтение ZIP/файлов. |
| `EpubOpfXml.kt` | Разбор OPF (spine, manifest). |
| `EpubMetadata.kt` | Обложка, автор, заголовок. |
| `EpubUriUtils.kt` | Разрешение путей внутри EPUB. |
| `EpubTypes.kt` | Вспомогательные типы слоя Android. |
| `EpubService.kt` | Публичный API операций с EPUB для читалки и импорта; использует [lib-kotlin-utils.md](lib-kotlin-utils.md) для таймаутов. |

## Поведение (актуальная реализация)

- Распаковка EPUB и таймауты копирования/unzip — в `EpubService.unpackThroughStep5()`; разбор **`container.xml` → OPF → spine** — в `open()` на **`Dispatchers.IO`** (`readOpfFromUnpackedRootFiles`, `buildSpineFromOpfXml`).
- **`EpubServiceError`** из низкоуровневого чтения OCF не маскируется общим текстом: пользователь видит конкретную причину (путь к OPF, отсутствие `full-path` и т.д.), если ошибка уже сформулирована как `EpubServiceError`.
- В `EpubIo.kt` для `container.xml` и OPF используется **устойчивое чтение UTF-8** (замена битых последовательностей), снятие **BOM**; разбор **`full-path`** допускает в том числе значение в **одинарных кавычках**.

Детальный разбор шагов и ошибок: [§4.2](../moduli-detail/sec-04-2-library-android-epub.md).

## Связи

| Направление | Кто |
|-------------|-----|
| Коды ошибок UI | [lib-kotlin-epub.md](lib-kotlin-epub.md) |
| Импорт | [lib-android-library.md](lib-android-library.md) |
| Читалка | [app-ui-reader.md](app-ui-reader.md) |
