---
moduli_section: "§3.5"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#35-карточка-книги-действия-верхняя-панель-первый-запуск"
tags: [BookCardSpec, BookActionsSheetSpec, AppTopBarSpec, FirstLaunchModalSpec]
---

# §3.5. Модуль `library-kotlin` — карточка, действия, топбар, первый запуск

Оглавление: [MODULI §3.5](../MODULI-I-KOMPONENTY.md#35-карточка-книги-действия-верхняя-панель-первый-запуск)

| Часть | Путь | Связи |
|-------|------|--------|
| Карточка книги | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/bookcard/BookCardSpec.kt` | `ChitalkaLibraryListPane` в `app`. |
| Лист действий | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/bookactions/BookActionsSheetSpec.kt` | Действия над книгой в списках. |
| Верхняя панель | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/topbar/AppTopBarSpec.kt` | `ChitalkaMainShell`. |
| Первый запуск | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/firstlaunch/FirstLaunchModalSpec.kt` | Модалка пустой библиотеки + `LibrarySessionState` ([§3.2](sec-03-2-library-kotlin-sessiya.md)). |

Тесты: `src/test/kotlin/com/chitalka/ui/bookcard/`, `bookactions/`, `topbar/`, `firstlaunch/`.
