# Внутренняя единица: конструктор и распаковка EPUB

**Родительский модуль:** `epub-service`  
**Файл кода:** `src/api/EpubService.ts`, класс `EpubService`

## Конструктор `new EpubService(epubFilePath)`

Сохраняет нормализованный URI через `ensureFileUri`.

## `getUnpackedRootUri()`

Возвращает URI корня распаковки после успешного unzip или `null`.

## `unpackThroughStep5()`

1. Копирование во внутренний кэш через [`util-android-copy-internal.md`](./util-android-copy-internal.md) + [`util-timeout-with-timeout.md`](./util-timeout-with-timeout.md) (`EPUB_ERR_TIMEOUT_COPY`).
2. Создание уникальной папки под `documentDirectory/book_cache/<id>/`.
3. `unzip` с таймаутом `EPUB_ERR_TIMEOUT_UNZIP`; при ошибке — попытка удалить папку.
4. Проверка `META-INF/container.xml`.

## Риски для агентов

Повторный вызов при уже распакованной книге — no-op по `unpackedRootUri`.
