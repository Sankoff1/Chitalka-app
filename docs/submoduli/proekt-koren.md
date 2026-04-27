---
id: proekt-koren
tags: [gradle, root-project, ci, detekt]
sostav_section: "Корень проекта"
---

# Корень проекта `chitalka-kotlin/`

## Расположение

Каталог **`chitalka-kotlin/`** в монорепозитории; не является отдельным артефактом APK/AAR, а **корневым Gradle-проектом**, объединяющим модули `app`, `library-android`, `library-compose`, `library-kotlin`.

## Назначение

- Задать **состав модулей** и общие настройки сборки.
- Зафиксировать **версии** зависимостей и параметры приложения (`APP_ID`, версии в `gradle.properties`).
- Подключить **качество кода** (Detekt), **CI** (`.github/workflows`), **Renovate** для обновления зависимостей.

## Ключевые файлы

| Файл | Роль |
|------|------|
| `settings.gradle.kts` | `include(...)` модулей |
| `build.gradle.kts` | Плагины/задачи уровня всего дерева |
| `gradle/libs.versions.toml` | Единый каталог версий для подмодулей |
| `gradle.properties` | Свойства, в т.ч. `APP_ID` для `app` |
| `gradlew` / `gradlew.bat` + `gradle/wrapper/*` | Одинаковая версия Gradle у всех |
| `config/detekt/detekt.yml` | Статический анализ Kotlin |
| `.github/` | Issue/PR шаблоны, workflows (см. ниже) |
| `README.md`, `TROUBLESHOOTING.md`, `LICENSE`, `CHITALKA_ORIGIN.txt` | Документация и лицензия |

### `.github/` (подробнее)

- `ISSUE_TEMPLATE/bug_report.md`, `ISSUE_TEMPLATE/feature_request.md`
- `PULL_REQUEST_TEMPLATE`
- `template-cleanup/README.md`
- `workflows/pre-merge.yaml` — проверки перед merge
- `workflows/publish-release.yaml`, `workflows/publish-snapshot.yaml` — публикация артефактов
- `workflows/cleanup.yaml` — очистка

### Вспомогательные каталоги (не код приложения)

- **`.claude/`** — локальные настройки/skills для Claude Code в этом репозитории; **не** входят в Gradle и APK. При необходимости описывайте отдельно в командной вики, не в `SOSTAV`.

## Связи

- **Все Gradle-модули** подчинены этому корню: см. [buildsrc.md](buildsrc.md), [app-koren.md](app-koren.md), [lib-kotlin-koren.md](lib-kotlin-koren.md), [lib-android-koren.md](lib-android-koren.md), [lib-compose.md](lib-compose.md).
- Оглавление кода: [MODULI-I-KOMPONENTY.md](../MODULI-I-KOMPONENTY.md), полный список файлов: [SOSTAV-VSEH-MODULEY-PUNKTAMI.md](../SOSTAV-VSEH-MODULEY-PUNKTAMI.md).
