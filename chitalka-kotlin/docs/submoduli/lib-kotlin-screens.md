---
id: lib-kotlin-screens
tags: [screen-spec, MVU-contract]
module: library-kotlin
package: com.chitalka.screens
---

# `library-kotlin` — подмодуль `com.chitalka.screens.*`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/` — подпапки `readingnow`, `books`, `favorites`, `trash`, `settings`, `debuglogs`, `reader`, `common`.

## Назначение

Декларативные **спеки экранов**: заголовки, ключи строк, подсказки, константы раскладки — чтобы Compose в `app` не дублировал бизнес-имена маршрутов RN.

## Связи

| Экран (логика) | Кто в `app` |
|----------------|-------------|
| Списки / корзина | [app-ui-library.md](app-ui-library.md) |
| Настройки (строки спека) | [app-ui-settings.md](app-ui-settings.md) |
| Логи | [app-ui-debug.md](app-ui-debug.md) |
| Читалка | [app-ui-reader.md](app-ui-reader.md) |

Общие: `BookListScreenLayout`, `BookListSearchFilter` — фильтрация/нормализация поиска для списков.

## Прочие связи

- [lib-kotlin-i18n.md](lib-kotlin-i18n.md) — ключи из спеков сопоставляются каталогу.
- [lib-kotlin-core-types.md](lib-kotlin-core-types.md) — данные списков.

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`screens/**/*Test`).
