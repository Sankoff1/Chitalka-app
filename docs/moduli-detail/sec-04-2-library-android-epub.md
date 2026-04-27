---
moduli_section: "§4.2"
module: library-android
source: "../MODULI-I-KOMPONENTY.md#42-epub-разбор-метаданные-импорт-в-библиотеку"
tags: [epub, zip, opf, metadata, import]
---

# §4.2. Модуль `library-android` — EPUB

Оглавление: [MODULI §4.2](../MODULI-I-KOMPONENTY.md#42-epub-разбор-метаданные-импорт-в-библиотеку)

| Часть | Путь | Связи |
|-------|------|--------|
| Сервис | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubService.kt` | Таймауты [§3.10](sec-03-10-library-kotlin-otladka.md); используется читалкой ([§2.4](sec-02-4-app-chitalka.md)) и импортом. |
| IO | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubIo.kt` | ZIP/потоки. |
| Метаданные | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubMetadata.kt` | Обложка, автор. |
| OPF | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubOpfXml.kt` | Манифест spine. |
| URI | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubUriUtils.kt` | Пути внутри EPUB; `ensureFileUri` нормализует `file:/C:/…` (результат `URI.resolve` на Windows) в `file:///C:/…`, иначе путь к OPF превращался в `file:///file:/C:/…` и метаданные при импорте терялись. |
| Типы | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubTypes.kt` | Модели слоя Android. |
| Импорт в библиотеку | `chitalka-kotlin/library-android/src/main/java/com/chitalka/library/ImportEpubToLibrary.kt` | `StorageService` ([§4.1](sec-04-1-library-android-hranilishche.md)), `I18nCatalog` ([§3.7](sec-03-7-library-kotlin-i18n-tema.md)), коды ошибок [§3.9](sec-03-9-library-kotlin-epub-oshibki.md). |

## Поток `EpubService`: распаковка и `open()`

1. **Копирование** входного `file://` / `content://` во внутренний `cacheDir/temp.epub` (`copySourceToTempEpub`, с таймаутом).
2. **Распаковка** ZIP в `filesDir/book_cache/<uuid>/` (`unzipArchiveToDirectory` на `Dispatchers.IO`, с таймаутом).
3. **Проверка OCF**: наличие файла `META-INF/container.xml` в корне распаковки.
4. **`open()`** после успешной распаковки:
   - чтение `container.xml` и OPF по `full-path` — функция `readOpfFromUnpackedRootFiles` в `EpubIo.kt`;
   - построение spine — `buildSpineFromOpfXml` в `EpubOpfXml.kt`.

Отладочные сообщения в лог (тег `[Chitalka][Epub]`) соответствуют шагам 1–7: шаги 1–5 покрывают копирование и unzip; шаг 6 — разбор OPF; шаг 7 — готовность spine.

## Потоки и ошибки

- **Диск при разборе структуры**: `readOpfFromUnpackedRootFiles` и `buildSpineFromOpfXml` вызываются из `EpubService.open()` внутри **`Dispatchers.IO`**, чтобы не выполнять чтение файлов и тяжёлый разбор XML на main thread.
- **`EpubServiceError`**: при ошибках уже уровня домена (нет `container.xml`, нет `full-path`, нет OPF по пути и т.п.) исключение **пробрасывается без замены текста** — в UI попадает конкретное сообщение из `EpubIo` / сервиса. Общее сообщение «Не удалось прочитать container.xml или OPF…» используется только как обёртка для **неожиданных** исключений (`IOException`, сбой декодера и т.д.), чтобы сохранить `cause` для диагностики.
- Соответствие кодов стабильных ошибок и строк i18n в читалке — в [§3.9](sec-03-9-library-kotlin-epub-oshibki.md) и `ReaderScreenSpec.epubOpenErrorMessage`.

## Чтение OCF XML (`EpubIo.kt`)

- Для **`META-INF/container.xml`** и файла **OPF** текст читается через **лениентный UTF-8**: невалидные последовательности заменяются символом замены (декодер `onMalformedInput` / `onUnmappableCharacter` = `REPLACE`), при полном сбое декодирования возможен откат к **ISO-8859-1** по байтам — чтобы регулярное выражение для `full-path` всё ещё могло сработать на ASCII-разметке.
- Перед разбором у строки снимается **BOM** (`U+FEFF`).
- Атрибут **`full-path`**: помимо общего шаблона с двойными/одинарными кавычками, поддержан явный вариант **`full-path='…'`**.

## Метаданные при импорте (`EpubMetadata.kt`)

`readFilesystemLibraryMetadata` по-прежнему вызывает тот же `readOpfFromUnpackedRootFiles`; при любой ошибке возвращаются пустые поля и `null` обложки (импорт не прерывается из-за метаданных). Для диагностики «немой» записи в библиотеке смотреть лог импорта и повторно открыть книгу в читалке — там ошибка EPUB не глотается.
