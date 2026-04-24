---
id: lib-kotlin-resources
tags: [resources, i18n-json, reader-js]
module: library-kotlin
path: library-kotlin/src/main/resources/
---

# `library-kotlin` — ресурсы JVM (`src/main/resources`)

## Расположение

`chitalka-kotlin/library-kotlin/src/main/resources/chitalka/`

| Путь | Назначение |
|------|------------|
| `i18n/ru.json`, `i18n/en.json` | Строки каталога для `I18nCatalog` — загружаются с classpath JAR модуля. |
| `reader/injectedScrollBridge.js` | Скрипт моста прокрутки/событий; может читаться и встраиваться в WebView из Android-кода. |

## Связи

| Потребитель | Зачем |
|-------------|--------|
| [lib-kotlin-i18n.md](lib-kotlin-i18n.md) | Парсинг JSON в каталог |
| [lib-kotlin-ui.md](lib-kotlin-ui.md) / [app-ui-reader.md](app-ui-reader.md) | Инъекция JS в читалку |

При добавлении локалей — новые файлы рядом с `ru.json` / `en.json` и обновление логики выбора в Kotlin.
