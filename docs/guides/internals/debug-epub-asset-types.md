# Внутренняя единица: декларация импорта `.epub`

**Родительский модуль:** `debug-epub-asset-types`  
**Файл кода:** `src/debug/epub-asset.d.ts`

## Назначение

Позволяет TypeScript принимать `import … from '*.epub'` как модуль: **default export — числовой asset id** (`number`), далее используется через `expo-asset` / `Asset.fromModule`.

## Связи

- [`debug-autoload-epub.md`](./debug-autoload-epub.md) импортирует демо-файл.

## Риски для агентов

В [`cfg-metro.md`](./cfg-metro.md) расширение `.epub` добавлено в `resolver.assetExts` рядом с `wasm`.
