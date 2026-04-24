---
moduli_section: "§2.5"
module: app
source: "../MODULI-I-KOMPONENTY.md#25-ресурсы-и-тесты-приложения"
tags: [android-resources, mipmap, strings, androidTest]
---

# §2.5. Модуль `app` — ресурсы и тесты

Оглавление: [MODULI §2.5](../MODULI-I-KOMPONENTY.md#25-ресурсы-и-тесты-приложения)

## Ресурсы

| Часть | Путь | Назначение |
|-------|------|------------|
| Строки, стили, цвета, размеры | `chitalka-kotlin/app/src/main/res/values/strings.xml`, `styles.xml`, `colors.xml`, `dimens.xml` | Локализация на уровне Android-ресурсов (дополняет каталог в `library-kotlin`, [§3.7](sec-03-7-library-kotlin-i18n-tema.md)). |
| Иконки launcher | `chitalka-kotlin/app/src/main/res/mipmap-*`, `drawable-*`, `mipmap-anydpi-v26/` | Иконка приложения, adaptive icon XML. |
| Демо EPUB для debug | `chitalka-kotlin/app/src/main/assets/debug/ebook.demo.epub` | Автозагрузка в библиотеку в debug-сборке ([§4.5](sec-04-5-library-android-otladka.md)). |

## Тесты

| Часть | Путь | Связи |
|-------|------|--------|
| Инструментальный тест | `chitalka-kotlin/app/src/androidTest/java/com/ncorti/kotlin/template/app/MainActivityTest.kt` | Запуск `MainActivity` ([§2.1](sec-02-1-app-zapusk.md)). |

## Сборка

Конфигурация артефакта: `chitalka-kotlin/app/build.gradle.kts` — см. обзор [modules/app.md](../modules/app.md).
