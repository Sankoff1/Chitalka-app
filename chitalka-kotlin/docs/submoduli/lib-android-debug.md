---
id: lib-android-debug
tags: [debug, auto-epub, debug-log-mirror]
module: library-android
package: com.chitalka.debug
---

# `library-android` — подмодуль `com.chitalka.debug`

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/debug/`

| Файл | Роль |
|------|------|
| `DebugAutoLoadEpubRunner.kt` | **`runDebugAutoLoadEpubIfNeeded`** — в debug-сборках (`BuildConfig.DEBUG` из `ChitalkaApp`) при выполнении условий подхватывает тестовый EPUB из `app/src/main/assets/debug/ebook.demo.epub` и вызывает импорт ([lib-android-library.md](lib-android-library.md)); правила — [lib-kotlin-debug.md](lib-kotlin-debug.md) `DebugAutoLoadEpub.kt`. |
| `ChitalkaMirrorLog.kt` | Обёртка над `android.util.Log`: дублирует сообщения в **`debugLogAppend`** (буфер из `library-kotlin`), т.к. `Log` пишет в logcat, а не в `System.out`. Используется вместо прямых вызовов `Log.*` внутри `library-android`. |

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [app-ui-yadro.md](app-ui-yadro.md) | `LaunchedEffect` в `ChitalkaApp` — автозагрузка EPUB |
| [app-ui-debug.md](app-ui-debug.md) | Косвенно: строки из кода Android, идущего через `ChitalkaMirrorLog`, попадают в панель отладочных логов |

Не путать **файл** `DebugLog.kt` (JVM, `library-kotlin`) с **пакетом** `com.chitalka.debug` в Android — разные артефакты, один префикс пакета.
