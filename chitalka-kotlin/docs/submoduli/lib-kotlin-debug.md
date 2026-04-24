---
id: lib-kotlin-debug
tags: [DebugLog, console-capture, auto-epub-rules]
module: library-kotlin
package: com.chitalka.debug
---

# `library-kotlin` — подмодуль `com.chitalka.debug`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/`

| Файл | Роль |
|------|------|
| `DebugLog.kt` | Кольцевой буфер отладочных записей, подписка/снимок для UI. |
| `InstallConsoleCapture.kt` | Перехват **stdout/stderr** в буфер (`println` и т.п.); вызывается из `Application`. Сообщения **`android.util.Log`** сюда **не** попадают — на Android их в буфер зеркалирует [lib-android-debug.md](lib-android-debug.md) (`ChitalkaMirrorLog`), сообщения **`console.*`** из WebView читалки — [app-ui-reader.md](app-ui-reader.md) (`ChitalkaReaderWebView`, `onConsoleMessage`). |
| `DebugAutoLoadEpub.kt` | Правила/данные для автозагрузки EPUB в debug (без Android Context). |

## Связи

| Направление | Кто |
|-------------|-----|
| UI логов | [app-ui-debug.md](app-ui-debug.md) |
| Запуск автоимпорта и зеркало `Log` на устройстве | [lib-android-debug.md](lib-android-debug.md) (`runDebugAutoLoadEpubIfNeeded`, `ChitalkaMirrorLog`) |
| Процесс | [app-vhod.md](app-vhod.md) — `installConsoleCapture` |

**Имя пакета** `com.chitalka.debug` используется и в Android (`library-android`) — не смешивать файлы JVM и Android при рефакторинге.

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md).
