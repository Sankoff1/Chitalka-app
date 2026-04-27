---
id: lib-kotlin-utils
tags: [WithTimeout, coroutines]
module: library-kotlin
package: com.chitalka.utils
---

# `library-kotlin` — подмодуль `com.chitalka.utils`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/utils/WithTimeout.kt`

## Назначение

Ограничение времени выполнения **suspend**-операций (`withTimeout` и исключения) — переиспользуемая утилита без привязки к Android.

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [lib-android-epub.md](lib-android-epub.md) | `EpubService` не зависает на долгих операциях |

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`WithTimeoutTest`).
