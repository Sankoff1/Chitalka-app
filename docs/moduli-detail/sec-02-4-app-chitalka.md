---
moduli_section: "§2.4"
module: app
source: "../MODULI-I-KOMPONENTY.md#24-открытие-книги-и-экран-читалки-нативная-оболочка-вокруг-web"
tags: [reader, WebView, Compose, ReaderRouteLifecycle, EpubService]
---

# §2.4. Модуль `app` — открытие книги и экран читалки

Оглавление: [MODULI §2.4](../MODULI-I-KOMPONENTY.md#24-открытие-книги-и-экран-читалки-нативная-оболочка-вокруг-web)

## Файлы и связи

| Часть | Путь | Связи |
|-------|------|--------|
| Маршрут читалки | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ReaderRouteScreen.kt` | `ReaderRouteLifecycle` ([§3.3](sec-03-3-library-kotlin-navigatsiya.md)), `refreshBookCount` из `library-android` ([§4.4](sec-04-4-library-android-prefs-sessiya.md)). |
| UI-модель маршрута | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ReaderRouteUiModel.kt` | Агрегирует `bookId`, `bookPath`, `LastOpenBookPersistence`, `LibrarySessionState`, `StorageService`. |
| Экран читалки | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ChitalkaReaderScreen.kt` | `ReaderScreenSpec` ([§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md)), `EpubService` ([§4.2](sec-04-2-library-android-epub.md)), мост [§3.6](sec-03-6-library-kotlin-most-chitalki.md), сохранение прогресса через `StorageService` ([§4.1](sec-04-1-library-android-hranilishche.md)). |
| WebView | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ChitalkaReaderWebView.kt` | Инъекция `ReaderBridge*` скриптов, `injectDarkReaderHead`, тема из `library-kotlin`; консоль страницы (`onConsoleMessage`) — в буфер отладочных логов ([§3.10](sec-03-10-library-kotlin-otladka.md) `DebugLog`). |
| Полифиллы | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReactNativeWebPolyfill.kt` | Совместимость ожиданий Web/RN-читалки с Android WebView. |

## Связь с навигацией

Параметры маршрута задаются в [§2.3](sec-02-3-app-navigatsiya.md) (`ChitalkaNavHost` + `AppNavRoutes`).
