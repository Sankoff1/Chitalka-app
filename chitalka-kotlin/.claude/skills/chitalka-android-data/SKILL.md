---
name: chitalka-android-data
description: >-
  Android library-android module: SQLite storage, EPUB import/parse, file
  picker, prefs, reader navigation coordination. Use when touching DB, EPUB,
  imports, or platform persistence.
---

# Chitalka — library-android (платформа)

Код: `chitalka-kotlin/library-android/src/main/java/com/chitalka/`.

## Хранилище

- `storage/StorageService.kt` — CRUD библиотеки и файлы; ошибки: `StorageServiceError.kt`.
- SQLite: `ChitalkaSqliteOpenHelper.kt`.
- Инструментальные тесты: `src/androidTest/.../storage/StorageServiceInstrumentedTest.kt`.

При смене схемы БД — миграции и обратная совместимость данных; проверь вызовы из `app` и обновление сессии.

## EPUB

- Сервис: `epub/EpubService.kt`; I/O: `EpubIo.kt`; метаданные: `EpubMetadata.kt`; OPF/XML: `EpubOpfXml.kt`; URI: `EpubUriUtils.kt`; типы: `EpubTypes.kt`.
- Импорт в библиотеку: `library/ImportEpubToLibrary.kt`.

Парсинг и файловые операции остаются в этом модуле; **коды ошибок** общего смысла — в `library-kotlin` (`EpubErrorCodes.kt`).

## Выбор файла и переход в читалку

- `picker/EpubPickerAndroid.kt` — Activity Result API.
- `navigation/ReaderNavCoordinator.kt` — координация перехода по bookId/path.

## Настройки и сессия

- `prefs/SharedPreferencesKeyValueStore.kt`.
- Обновление состояния библиотеки после операций: `library/LibrarySessionRefresh.kt`.

## Отладка

- `debug/DebugAutoLoadEpubRunner.kt` — runner автозагрузки EPUB на устройстве.

## Наследие шаблона

`com.ncorti.kotlin.template.library.android.ToastUtil` — шаблон; не развивать как часть домена Chitalka.
