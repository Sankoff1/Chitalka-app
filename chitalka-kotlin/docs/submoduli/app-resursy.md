---
id: app-resursy
tags: [android-resources, manifest, launcher]
module: app
path: app/src/main/res/, AndroidManifest
---

# Модуль `app/` — ресурсы и манифест

## Расположение

- `chitalka-kotlin/app/src/main/AndroidManifest.xml`
- `chitalka-kotlin/app/src/main/res/**`

## Назначение

| Зона | Роль |
|------|------|
| `AndroidManifest.xml` | Регистрация `Application`, `MainActivity`, тема окна, FileProvider при необходимости. |
| `res/values/*` | Строки, стили, цвета, dimens для Android/XML-части (дополняют строки каталога в `library-kotlin`). |
| `res/layout/activity_main.xml` | Разметка под классическую Activity (если используется вместе с Compose). |
| `res/xml/chitalka_file_paths.xml` | Пути для `FileProvider` / обмена файлами. |
| `res/mipmap-*`, `res/drawable-*` | Иконка приложения (включая adaptive icon). |

## Связи

| Кто использует | Зачем |
|----------------|--------|
| [app-vhod.md](app-vhod.md) | Точки входа объявлены в манифесте |
| Система Android | Ресурсы по квалификаторам плотности и API |
| `library-kotlin` i18n | Параллельный слой строк для JVM-каталога — см. [lib-kotlin-resources.md](lib-kotlin-resources.md) |
