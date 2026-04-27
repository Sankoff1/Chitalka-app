---
id: app-testy
tags: [androidTest, MainActivity]
module: app
path: app/src/androidTest/
---

# Модуль `app/` — инструментальные тесты

## Расположение

`chitalka-kotlin/app/src/androidTest/java/com/ncorti/kotlin/template/app/MainActivityTest.kt`

## Назначение

- Проверка запуска **`MainActivity`** на устройстве/эмуляторе (интеграция с AndroidX Test).

## Связи

| Кто | Связь |
|-----|--------|
| [app-vhod.md](app-vhod.md) | Тестируемая Activity |
| [app-koren.md](app-koren.md) | `testInstrumentationRunner` задаётся в `build.gradle.kts` модуля |
