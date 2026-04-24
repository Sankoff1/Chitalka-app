# Доменные данные: SQLite, библиотека, EPUB-пайплайн

Краткая карта для агентов: что хранится в БД, как устроены типы записей, как `EpubService` и импорт работают с файлами на диске, зачем копирование во внутреннее хранилище на Android и как интерпретировать результат выбора файла.

## SQLite: ментальная модель схемы

Файл БД: логическое имя `chitalka.db` (управляется `expo-sqlite`, `openDatabaseAsync`). Схема создаётся при первом открытии через `CREATE TABLE IF NOT EXISTS` в `StorageService`.

| Таблица | Назначение |
|--------|------------|
| **`reading_progress`** | Одна строка на книгу: последняя позиция чтения (глава по индексу spine, вертикальный скролл, время). PK — `book_id`. |
| **`library_books`** | Каталог добавленных в библиотеку книг: путь к EPUB, метаданные, размер, обложка, дата добавления. PK — `book_id`. |

Индексы:

- `idx_reading_progress_last_read` по `last_read_timestamp DESC` — удобная сортировка «недавно читали».
- `idx_library_books_added` по `added_at DESC` — список библиотеки по дате добавления.

Операция «очистить данные» (`clearAllData`) удаляет **все** строки из обеих таблиц (и прогресс, и библиотеку).

## `ReadingProgress` и `LibraryBookRecord` (поля)

Типы объявлены в `src/core/types.ts`; в SQLite колонки именованы в `snake_case`, в TypeScript — в `camelCase` (маппинг в запросах `StorageService`).

### `ReadingProgress`

| Поле | Смысл |
|------|--------|
| `bookId` | Идентификатор книги (строка, не пустая). Совпадает с ключом в библиотеке и с тем, что передаётся в ридер. |
| `lastChapterIndex` | Индекс элемента **spine** (целое; при сохранении усекается `Math.trunc`). Соответствует порядку глав в разобранном EPUB. |
| `scrollOffset` | Вертикальная позиция прокрутки в WebView (число, `REAL` в БД). |
| `lastReadTimestamp` | Unix-время последнего сохранения прогресса (мс, в БД `INTEGER`, при записи усекается). |

### `LibraryBookRecord`

| Поле | Смысл |
|------|--------|
| `bookId` | Уникальный id книги в приложении (часто производится от имени файла при импорте). |
| `fileUri` | Стабильный URI файла EPUB в **постоянном** каталоге приложения (`documentDirectory/library_epubs/…`), не временный picker URI. |
| `title`, `author` | Метаданные из OPF; при пустых значениях на импорте подставляются локализованные заглушки. |
| `fileSizeBytes` | Размер файла EPUB в байтах (≥ 0, при записи усекается). |
| `coverUri` | `file://` к скопированной обложке в `library_covers/`, либо `null`. |
| `addedAt` | Время добавления записи (мс, `Date.now()` при импорте). |

## `EpubService`: пайплайн, пути, ошибки, таймауты

Модуль: `src/api/EpubService.ts`. Роль: распаковка локального EPUB без epubjs `book.ready`, разбор OPF (spine), подготовка HTML главы для WebView (`prepareChapter` — чтение файла, переписывание локальных `img src` в абсолютные `file://`, инъекция viewport/reflow CSS).

### Публичные сущности, которые полезно знать агенту

- **`EpubService`** — класс: конструктор принимает путь/URI к `.epub`; методы `unpackThroughStep5`, `open`, `getSpineChapterUri`, `prepareChapter`, `getMetadata`, `resolveCoverFileUri`, `destroy`, геттер `getUnpackedRootUri`.
- **`EpubServiceError`** — ошибки сервиса; часто `message` совпадает с машинными константами ниже.
- **`readFilesystemLibraryMetadata(unpackedRootUri)`** — только файловая система: title/author из OPF и URI обложки (для импорта в библиотеку без полного `open()`).
- Типы: **`EpubStructure`**, **`EpubSpineItem`**, **`EpubTocItem`** (TOC в `open()` пока отдаётся пустым массивом).

### Пути распаковки (чтение в ридере)

1. **Исходный EPUB** — `epubSourceUri` после нормализации (`ensureFileUri`), в т.ч. починка префикса `null/` (баг epubjs/URL на Android).

2. **Шаги 1–5 (`unpackThroughStep5`)**  
   - Копия во **внутренний кэш**: `copyFileToInternalStorage` → `cacheDirectory/temp.epub` (см. следующий раздел).  
   - Каталог распаковки: **`documentDirectory` + `book_cache/` + `<uuid или timestamp_random>/`**.  
   - Нативный `unzip(epubPath, destPath)` получает **нативные пути** (без `file://`), через `fileUriToNativePath`.  
   - После unzip проверяется наличие **`META-INF/container.xml`** в корне распаковки.

3. **`EpubStructure.unpackedRootUri`** — это каталог распаковки (как строка URI каталога), логически «корень книги»; для epubjs-подобных ссылок OPF лежит в подкаталоге, база для глав — `opfDirFileUrl` внутри сервиса.

### Таймауты (`withTimeout`)

Все значения в миллисекундах; при срабатывании в промис отклонения попадает `Error` с **точным** `message`, равным константе ошибки (исходная операция в фоне не отменяется).

| Константа | Мс | Где используется |
|-----------|-----|-------------------|
| `EPUB_ERR_TIMEOUT_COPY` | 180 000 | Копирование во внутренний `temp.epub` в `unpackThroughStep5`. |
| `EPUB_ERR_TIMEOUT_UNZIP` | 600 000 | Распаковка ZIP/EPUB. |
| `EPUB_ERR_TIMEOUT_PREPARE_CHAPTER` | 180 000 | Чтение и обработка HTML главы в `prepareChapter`. |

### Коды / строки ошибок, важные для UI

- **`EPUB_EMPTY_SPINE`** — строковое значение **`'EMPTY_SPINE'`** (экспорт константы `EPUB_EMPTY_SPINE`). Выбрасывается из `open()`, если после разбора OPF массив spine пуст.  
  **`ReaderScreen`** сопоставляет `error.message` с этой константой (и с таймаутами) и подставляет **локализованные** строки через `t('reader.errors.emptySpine')` и т.д. Дополнительно экран после `open()` проверяет `structure.spine.length` и при нуле снова кидает `EpubServiceError(EPUB_EMPTY_SPINE)` — поведение согласовано с маппингом ошибок.
- Таймауты: `EPUB_ERR_TIMEOUT_COPY`, `EPUB_ERR_TIMEOUT_UNZIP`, `EPUB_ERR_TIMEOUT_PREPARE_CHAPTER` — те же строки в `message` у `EpubServiceError` после обёртки.
- Прочие сообщения — человекочитаемые русские строки из `EpubServiceError` (нет отдельного enum); `ReaderScreen` для `EpubServiceError` без совпадения с известными ключами показывает `error.message` или fallback `reader.errors.openFailed`.

### `withTimeout` (`src/utils/withTimeout.ts`)

`Promise.race` между исходным промисом и таймером. **Отмены нативной работы нет** — при таймауте UI получает отказ, долгая операция может ещё идти. Таймер снимается в `finally`.

## Импорт в библиотеку: `importEpubToLibrary`

Файл: `src/library/importEpubToLibrary.ts`. Функция **`importEpubToLibrary(sourceUri, bookId, storage, locale, options?)`** → `{ stableUri, bookId }`.

### Порядок шагов

1. Проверка `FileSystem.documentDirectory`.
2. Подготовка имён: «stem» из `bookId` (санитизация) + короткий суффикс от хэша id → базовое имя файла `fileBase`.
3. Каталоги: **`documentDirectory/library_epubs/`** и **`documentDirectory/library_covers/`** — создаются с `intermediates: true`.
4. Целевой EPUB: **`stableUri` = `library_epubs/<fileBase>.epub`**.
5. **`copyFileToInternalStorage(sourceUri)`** → `cacheDirectory/temp.epub`, затем **`copyAsync`** в `stableUri`, затем попытка удалить временный `temp.epub`.
6. Размер файла через `getInfoAsync(stableUri)`.
7. **`new EpubService(stableUri)`** → **`unpackThroughStep5()`** (временная распаковка в `book_cache/<id>/` — отдельно от постоянного EPUB).
8. **`readFilesystemLibraryMetadata(unpacked)`** — заголовок, автор, путь к обложке внутри распаковки.
9. При наличии обложки — копия в **`library_covers/<fileBase>_cover<ext>`** (`ext` по расширению исходного файла обложки).
10. **`storage.addBook(row)`** (`LibraryBookRecord` с `fileUri: stableUri`, заглушки из `bookFallbackLabels(locale)` для пустых title/author).
11. В `finally` у сервиса вызывается **`destroy()`** (сброс in-memory состояния; каталог `book_cache` от распаковки импорта остаётся на диске, если приложение его не чистит отдельно — это побочный кэш unzip).

Успех: опционально `Alert` «Книга добавлена в базу» (если не `suppressSuccessAlert`).

### Итоговая раскладка под `documentDirectory`

| Путь (относительно `documentDirectory`) | Содержимое |
|------------------------------------------|------------|
| `library_epubs/<stem>__<suffix>.epub` | Постоянная копия EPUB для списка библиотеки и открытия ридера. |
| `library_covers/<stem>__<suffix>_cover.<ext>` | Стабильная обложка для UI (если удалось скопировать). |
| `book_cache/<uuid>/` | Используется **`EpubService`** при чтении и при импорте на этапе метаданных; не то же самое, что `library_epubs`. |

## Зачем `copyFileToInternalStorage` (Android и не только)

Файл: `src/utils/epubPipelineAndroid.ts` (имя историческое; логика универсальна для Expo FS).

**Задача:** привести любой поддерживаемый **`from`** URI (в т.ч. **`content://`** от DocumentPicker без копирования в кэш Expo) к **обычному файлу** `file://` в **`cacheDirectory/temp.epub`**.

Так дальше **`FileSystem.copyAsync` / нативный `unzip`** работают с предсказуемым путём **без** постоянных обращений к провайдеру контента и без запроса отдельных разрешений к «внешнему» хранилищу в типичном сценарии. Комментарий в `EpubService`: epubjs/URL на Android дают некорректные URI с префиксом `null/` — нормализация в том же модуле дополняет эту линию защиты.

## Выбор EPUB: `epubPicker`

Файл: `src/utils/epubPicker.ts`.

### Варианты результата (`EpubPickResult`)

| `kind` | Когда | Поля |
|--------|--------|------|
| **`ok`** | Пользователь выбрал документ, похожий на EPUB | `uri` — URI ассета (на Android часто `content://`), `bookId` — из имени файла через **`deriveBookId`** (имя без `.epub`, иначе fallback `book_<timestamp>`). |
| **`canceled`** | Явная отмена picker или пустой `uri`/ассет. |
| **`error`** | Неверный тип файла или исключение при открытии picker | Только **`messageKey`** — не готовая строка для пользователя. |

### Поле `messageKey`

Строковые ключи для последующего перевода через i18n (например `useI18n().t(messageKey)` или общий каталог сообщений):

- **`picker.invalidExtension`** — ассет не похож на EPUB (имя, MIME или путь без `.epub` и без epub в MIME).
- **`picker.openFailed`** — исключение вокруг `DocumentPicker.getDocumentAsync`.

На Android массив MIME-типов для picker шире (`application/epub+zip`, `octet-stream`, FictionBook, `*/*`); на iOS тип **`application/epub+zip`**. **`copyToCacheDirectory: false`** — осознанный выбор: на Android надёжнее своя копия через `copyFileToInternalStorage` / импорт в `documentDirectory`.

---

**Вспомогательно для агентов:** при правках сообщений об ошибках в ридере сохраняйте равенство `error.message` константам из `EpubService`, иначе сломается ветка локализованных `reader.errors.*`.
