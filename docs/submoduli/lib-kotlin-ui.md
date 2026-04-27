---
id: lib-kotlin-ui
tags: [ui-spec, readerview, BookCard]
module: library-kotlin
package: com.chitalka.ui
---

# `library-kotlin` — подмодуль `com.chitalka.ui.*`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/` — подпакеты `bookcard`, `bookactions`, `topbar`, `firstlaunch`, `readerview`.

## Назначение

| Подпакет | Роль |
|----------|------|
| `bookcard` | Контракт карточки книги в списке. |
| `bookactions` | Действия с книгой (нижний лист). |
| `topbar` | Спецификация верхней панели. |
| `firstlaunch` | Модалка первого запуска / пустой библиотеки. |
| `readerview` | **Мост WebView**: JS, сообщения, тёмный HTML, направление страниц. |

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [app-ui-library.md](app-ui-library.md) | Карточка, действия |
| [app-ui-yadro.md](app-ui-yadro.md) | Top bar, first launch в shell |
| [app-ui-reader.md](app-ui-reader.md) | Весь `readerview` |

Ресурс JS: [lib-kotlin-resources.md](lib-kotlin-resources.md) (`injectedScrollBridge.js`).

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`ui/**/*Test`).
