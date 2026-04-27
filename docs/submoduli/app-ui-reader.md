---
id: app-ui-reader
tags: [reader, WebView, Compose, epub]
module: app
path: app/.../ui/reader/
---

# Модуль `app/` — подмодуль `ui/reader/`

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/`

| Файл | Назначение |
|------|------------|
| `ChitalkaReaderScreen.kt` | Основной UI читалки: загрузка книги, `EpubService`, сохранение прогресса, обработка моста WebView, спека `ReaderScreenSpec`. |
| `ChitalkaReaderWebView.kt` | Настройка `WebView`, инъекция JS моста, тёмная тема страницы; `onConsoleMessage` пересылает `console.*` в буфер отладки (`debugLogAppend`, см. [app-ui-debug.md](app-ui-debug.md)). |
| `ReactNativeWebPolyfill.kt` | Полифиллы для ожиданий RN/Web-читалки в среде Android WebView. |

## Связи

| Направление | Кто |
|-------------|-----|
| → | [lib-kotlin-screens.md](lib-kotlin-screens.md) (`ReaderScreenSpec`), [lib-kotlin-ui.md](lib-kotlin-ui.md) (`readerview/*`), [lib-kotlin-theme.md](lib-kotlin-theme.md), [lib-android-epub.md](lib-android-epub.md) (`EpubService`), [lib-android-storage.md](lib-android-storage.md) |
| ← | [app-ui-yadro.md](app-ui-yadro.md) — `ReaderRouteScreen` открывает этот экран |
