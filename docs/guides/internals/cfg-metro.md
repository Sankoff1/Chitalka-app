# Внутренняя единица: `metro.config.js`

**Родительский модуль:** `cfg-metro`  
**Файл:** `metro.config.js`

## База

`getDefaultConfig(__dirname)` из `expo/metro-config`.

## Расширения ассетов

- `wasm` — для web-ветки `expo-sqlite` / wa-sqlite.
- `epub` — чтобы бандлился демо EPUB ([`debug-bundled-epub-asset.md`](./debug-bundled-epub-asset.md)).

## Риски для агентов

Удаление `epub` из `assetExts` сломает dev-автозагрузку и импорт ассета.
