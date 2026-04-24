---
moduli_section: "§3.1"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#31-типы-данных-библиотеки-и-прогресса-чтения"
tags: [domain, core-types, LibraryBookRecord, ReadingProgress]
---

# §3.1. Модуль `library-kotlin` — типы данных библиотеки и прогресса

Оглавление: [MODULI §3.1](../MODULI-I-KOMPONENTY.md#31-типы-данных-библиотеки-и-прогресса-чтения)

Пакет: `com.chitalka.core.types` — без Android SDK.

| Часть | Путь | Кто потребляет |
|-------|------|----------------|
| Запись книги | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/LibraryBookRecord.kt` | `StorageService` ([§4.1](sec-04-1-library-android-hranilishche.md)), импорт EPUB ([§4.2](sec-04-2-library-android-epub.md)), UI списков в `app`. |
| Книга + прогресс | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/LibraryBookWithProgress.kt` | Списки в `app`, операции чтения. |
| Прогресс чтения | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/ReadingProgress.kt` | Читалка, БД. |

Зависимостей на другие модули проекта нет. См. также [§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md) (экраны используют эти типы).
