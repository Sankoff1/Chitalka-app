---
id: lib-android-navigation
tags: [ReaderNavCoordinator, pending-navigation]
module: library-android
package: com.chitalka.navigation
---

# `library-android` — подмодуль `com.chitalka.navigation`

## Расположение

`chitalka-kotlin/library-android/src/main/java/com/chitalka/navigation/ReaderNavCoordinator.kt`

## Назначение

Координатор перехода в экран **Reader**, если `NavHost` ещё не готов: хранит `ReaderRouteParams`, повторяет попытки на main thread (аналог RN `navigationRef` + pending).

## Связи

| Направление | Кто |
|-------------|-----|
| Параметры маршрута | [lib-kotlin-navigation.md](lib-kotlin-navigation.md) `ReaderRouteParams` |
| Встраивание | [app-ui-yadro.md](app-ui-yadro.md) `rememberReaderNavCoordinator`, `ReaderNavCoordinatorSideEffects` |
| Открытие из UI | [app-ui-yadro.md](app-ui-yadro.md) `ChitalkaAppController` |
