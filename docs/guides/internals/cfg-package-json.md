# Внутренняя единица: `package.json`

**Родительский модуль:** `cfg-package`  
**Файл:** `package.json`

## Ключевые поля

- `main`: точка входа (`index.ts`).
- `scripts`: `expo start`, `expo run:*`, `postinstall` → `patch-package`.
- `dependencies`: Expo SDK, навигация, SQLite, WebView, zip и т.д.

## Связи

- Версии должны быть согласованы с документацией Expo для текущего SDK.

## Риски для агентов

`postinstall` обязателен при наличии патчей в `patches/`.
