---
moduli_section: "§3.2"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#32-сессия-библиотеки-и-восстановление-последней-открытой-книги"
tags: [LibrarySessionState, LastOpenBook, LibraryBookLookup, restore]
---

# §3.2. Модуль `library-kotlin` — сессия и последняя открытая книга

Оглавление: [MODULI §3.2](../MODULI-I-KOMPONENTY.md#32-сессия-библиотеки-и-восстановление-последней-открытой-книги)

| Часть | Путь | Связи |
|-------|------|--------|
| Сессия | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LibrarySessionState.kt` | Держится в `ChitalkaApp` ([§2.2](sec-02-2-app-compose-koren.md)); счётчик книг обновляется расширением `refreshBookCount` в [§4.4](sec-04-4-library-android-prefs-sessiya.md). |
| Поиск книги | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LibraryBookLookup.kt` | Интерфейс; реализация — `StorageService` ([§4.1](sec-04-1-library-android-hranilishche.md)). |
| Last-open id | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LastOpenBook.kt` | Ключи и чтение/запись id; персистентность через `LastOpenBookPersistence` → [§4.4](sec-04-4-library-android-prefs-sessiya.md). |
| Восстановление читалки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LastOpenReaderRestore.kt` | `restoreLastOpenReaderIfNeeded` вызывается из `ChitalkaApp`; нужен `LibraryBookLookup` + persistence. |

Тесты: `chitalka-kotlin/library-kotlin/src/test/kotlin/com/chitalka/library/*`.
