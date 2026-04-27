---
id: lib-kotlin-testy
tags: [unit-test, junit]
module: library-kotlin
path: library-kotlin/src/test/
---

# `library-kotlin` — тесты

## Расположение

- `chitalka-kotlin/library-kotlin/src/test/kotlin/com/chitalka/**` — зеркало пакетов домена, файлы `*Test.kt`.
- `chitalka-kotlin/library-kotlin/src/test/java/com/ncorti/kotlin/template/library/FactorialCalculatorTest.kt` — тест шаблона.

## Назначение

Проверка **чистой JVM-логики**: навигация, сессия библиотеки, спеки экранов, i18n, тема, мост читалки (парсинг сообщений, HTML), picker utils, таймауты, отладочный лог.

## Связи

Каждый тест привязан к соответствующему подмодулю `src/main`: см. список в [SOSTAV-VSEH-MODULEY-PUNKTAMI.md](../SOSTAV-VSEH-MODULEY-PUNKTAMI.md) раздел «Тесты — library-kotlin».

Запуск: задачи Gradle `library-kotlin` test (из корня проекта [proekt-koren.md](proekt-koren.md)).
