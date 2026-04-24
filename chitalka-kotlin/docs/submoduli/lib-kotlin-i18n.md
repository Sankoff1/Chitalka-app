---
id: lib-kotlin-i18n
tags: [i18n, AppLocale, I18nCatalog]
module: library-kotlin
package: com.chitalka.i18n
---

# `library-kotlin` — подмодуль `com.chitalka.i18n`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/`

| Файл | Роль |
|------|------|
| `I18nTypes.kt` | Типы локали и вспомогательные enum/data. |
| `I18nCatalog.kt` | Доступ к строкам по ключу для выбранной локали. |
| `I18nPreferences.kt` | `I18nUiState`, загрузка/сохранение предпочтения языка (функции уровня модуля; хранилище подставляет `app` + Android). |

## Связи

| Направление | Кто |
|-------------|-----|
| Ресурсы JSON | [lib-kotlin-resources.md](lib-kotlin-resources.md) — `ru.json`, `en.json` |
| Android prefs | [lib-android-prefs.md](lib-android-prefs.md) — реализация персистентности |
| App UI | [app-ui-yadro.md](app-ui-yadro.md), [app-ui-settings.md](app-ui-settings.md), списки [app-ui-library.md](app-ui-library.md) |
| Импорт EPUB | [lib-android-library.md](lib-android-library.md) использует каталог для сообщений |

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`i18n/*Test`).
