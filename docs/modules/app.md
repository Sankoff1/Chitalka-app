---
module: app
type: android-application
namespace: com.ncorti.kotlin.template.app
gradle: chitalka-kotlin/app/build.gradle.kts
tags:
  - android-app
  - jetpack-compose
  - navigation-compose
  - material3
  - webview-reader
consumes:
  - library-kotlin
  - library-android
consumed_by: []
peers: []
---

# Модуль `app`

Теги: `#android-app` `#compose` `#navigation` `#drawer` `#reader-route` `#epub-import` `#settings` `#debug-ui`

Android-приложение Chitalka: единственная точка сборки APK, весь пользовательский Compose UI, обвязка вокруг `NavHost`, drawer, панелей библиотеки и экрана читалки.

---

## Gradle и конфигурация

| Что | Путь |
|-----|------|
| Скрипт модуля | `chitalka-kotlin/app/build.gradle.kts` |
| `applicationId` / `namespace` | задаются из `chitalka-kotlin/gradle.properties` (`APP_ID`, версии) |
| ProGuard | `chitalka-kotlin/app/proguard-rules.pro` |

**Зависимости проекта:** `implementation(projects.libraryAndroid)`, `implementation(projects.libraryKotlin)` — **`library-compose` не подключён.**

Внешние ключевые библиотеки: Compose BOM, Navigation Compose, Lifecycle, Coil.

---

## Связи с другими модулями

| Направление | Суть связи |
|-------------|------------|
| → `library-kotlin` | Типы навигации (`RootStackRoutes`, `DrawerScreen`, спеки экранов, i18n/theme, мост читалки, доменные типы книг, `LibrarySessionState`, восстановление читалки `restoreLastOpenReaderIfNeeded`). |
| → `library-android` | `StorageService`, `SharedPreferencesKeyValueStore`, `EpubPickerAndroid`, `importEpubToLibrary`, `EpubService` / URI-хелперы, `ReaderNavCoordinator`, `LibrarySessionState.refreshBookCount` (расширение), `runDebugAutoLoadEpubIfNeeded`, `ChitalkaMirrorLog` (зеркало `Log` в буфер отладки); `installConsoleCapture` подключается в `Application` из `library-kotlin`. |

**Расширения из `library-android` в пакете `com.chitalka.library`:** приложение импортирует их вместе с классами из `library-kotlin` того же пакета — на classpath они объединяются (общий пакет `com.chitalka.library` / `com.chitalka.debug` и т.д.).

---

## Поток UI (высокий уровень)

```mermaid
flowchart TB
  MainActivity --> ChitalkaApp
  ChitalkaApp --> ChitalkaNavHost
  ChitalkaNavHost -->|route Main| ChitalkaMainShell
  ChitalkaNavHost -->|route Reader| ReaderRouteScreen
  ChitalkaMainShell --> ChitalkaDrawerRouter
  ChitalkaDrawerRouter --> ChitalkaLibraryListPane
  ChitalkaDrawerRouter --> ChitalkaTrashPane
  ChitalkaDrawerRouter --> ChitalkaDebugLogsPane
  ChitalkaDrawerRouter --> ChitalkaSettingsPane
  ReaderRouteScreen --> ChitalkaReaderScreen
  ChitalkaReaderScreen --> ChitalkaReaderWebView
```

---

## Иерархия composable и навигация

| Узел | Роль | Путь |
|------|------|------|
| `ChitalkaApp` | Создаёт `StorageService`, `SharedPreferencesKeyValueStore`, `LibrarySessionState`, `NavController`, координатор читалки, импорт EPUB, тему и оборачивает `ChitalkaNavHost`. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaApp.kt` |
| `rememberReaderNavCoordinator` / `ReaderNavCoordinatorSideEffects` | Связка `ReaderNavCoordinator` (из `library-android`) с `NavHostController` и lifecycle. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaNavigationSetup.kt` |
| `ChitalkaAppController` | Тонкая обёртка: `openReader` → координатор; `bumpLists` — инкремент nonce списков. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaAppController.kt` |
| `ChitalkaNavHost` | Два маршрута: `Main` → контент shell; `Reader/{bookId}/{bookPath}` → `ReaderRouteScreen`. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaNavHost.kt` |
| `AppNavRoutes` | Строки маршрутов и `navigateToReader` (encode URI). Использует `RootStackRoutes` из Kotlin-модуля. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/AppNavRoutes.kt` |
| `ChitalkaMainShell` | `ModalNavigationDrawer` + `Scaffold`, состояние выбранного `DrawerScreen`, `searchQuery`/`isSearchOpen`, welcome-флаги; собирает `ChitalkaDrawerContent` / `ChitalkaTopBar` / `ShellContent` / `WelcomeDialog`. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaMainShell.kt` |
| `ChitalkaTopBar` + `CompactSearchField` | Top bar с логикой `AppTopBarSpec` (search-input vs title, кнопки меню/поиска/очистки) и компактное поле поиска на `onPrimary`-подложке. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaTopBar.kt` |
| `ChitalkaDrawerContent` + `WelcomeDialog` + `DrawerScreen.icon()` | Содержимое drawer (`ModalDrawerSheet` + `NavigationDrawerItem` по `DrawerNavigationSpec.drawerScreenOrder`), first-launch диалог выбора EPUB, маппинг `DrawerScreen → ImageVector`. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaShellPieces.kt` |
| `ChitalkaDrawerRouter` | `when (DrawerScreen)` → конкретные панели. | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaDrawerRouter.kt` |

---

## Экран читалки и WebView

| Файл | Роль |
|------|------|
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ReaderRouteScreen.kt` | Маршрут читалки: lifecycle из `ReaderRouteLifecycle`, обновление счётчика книг. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ReaderRouteUiModel.kt` | Параметры маршрута + `persistence`, `librarySession`, `storage`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ChitalkaReaderScreen.kt` | Точка входа читалки: `remember { ReaderScreenState(...) }`, `LaunchedEffect` для `initialize` + получения `bookRecord`, `DisposableEffect` для `dispose`, диспетчинг `when (state.phase)` по Error/Loading/Ready. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReaderScreenState.kt` | `ReaderLoadPhase`, `@Stable class ReaderScreenState` с Compose-state-полями (phase, layerA/B, activeLayerId, transition*, busy, bookRecord, transitionProgress Animatable) и методами `activeLayer()` / `dispose()`. Helper `openErrorText(locale, e)` — маппинг `EpubServiceError` / прочих ошибок на строки из `ReaderScreenSpec`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReaderScreenStateOps.kt` | Extension-функции над `ReaderScreenState`: `schedulePersist` / `persistNow`, `goToChapter` (переключение слоя A/B с `CompletableDeferred` gate и анимацией `transitionProgress`), `handleBridge` (Ready / Scroll debounce / Page next/prev), `initialize` (EPUB open, восстановление `savedIndex` / `scroll`, сохранение прогресса + `librarySession.refreshBookCount`). |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReaderPageLayer.kt` | Private composable `ReaderPageLayer` с двухслойным кроссфейдом (alpha + translationX + затемнение shade) и хелпер `parseThemeColor(hex)` для `ThemeColors` в `Color`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReaderPhaseContent.kt` | Три private composable'а по фазам: `ReaderErrorContent`, `ReaderLoadingContent`, `ReaderReadyContent` (Scaffold с TopAppBar + bottomBar с `pageIndicatorSlash`; рендер обоих `ReaderPageLayer` A и B). |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ChitalkaReaderWebView.kt` | Настройка WebView, инъекция скриптов моста, тёмная тема страницы; `WebChromeClient.onConsoleMessage` дублирует `console.*` страницы в `debugLogAppend` (вкладка отладочных логов). |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReactNativeWebPolyfill.kt` | Совместимость с ожиданиями Web/RN-читалки. |

---

## Панели библиотеки, настройки, отладка

| Файл | Роль |
|------|------|
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/library/ChitalkaLibraryListPane.kt` | Списки «сейчас читаю» / «все книги» / избранное: оркестрация `LazyColumn` + `ModalBottomSheet`, `EmptyLibraryState`. Загрузка из БД зависит только от `listRefreshNonce`; поиск фильтрует уже загруженный список через `remember(rawBooks, normalizedSearchQuery)` — ввод в поиске не ходит в SQLite. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/library/BookRowCard.kt` | Карточка книги: `BookRowCard` (Card + cover + title/author/favorite + меню) и приватные `ReadingProgressBlock`, `BookCover` — Layout-константы и i18n-ключи через `BookCardSpec`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/library/BookActionsContent.kt` | Контент `ModalBottomSheet`: `BookActionsContent` (favorite toggle / move-to-trash / cancel) + приватный `ActionRow`; вызывает `storage.setBookFavorite` и `storage.moveBookToTrash`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/library/ChitalkaTrashPane.kt` | Корзина: `TrashScreenSpec`, удалённые книги. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/settings/ChitalkaSettingsPane.kt` | Локаль и тема: `I18nUiState`, `persistLocale` / `persistThemeMode`; внутри — `SettingsCard`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/settings/LanguageDropdown.kt` | Кастомный dropdown выбора локали (trigger + `Popup` + строки локалей через `APP_LOCALES`); используется только из `ChitalkaSettingsPane`. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/debug/ChitalkaDebugLogsPane.kt` | Подписка на `debugLog*` из `library-kotlin`; очистка, копирование в буфер обмена (`ClipboardManager`), экспорт файла; подписи через `DebugLogsScreenSpec`. Перестроение списка по подписке coalesc'ится через `AtomicBoolean` + `delay(DEBUG_LOG_RELOAD_COALESCE_MS)`, чтобы шторм `console.*` из WebView не триггерил `reload()` per-line. |

---

## Тема и CompositionLocal

| Файл | Роль |
|------|------|
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/theme/ChitalkaTheme.kt` | `ChitalkaMaterialTheme`, связка с `ThemeColors` / `ThemeMode` из Kotlin-модуля. |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaCompositionLocals.kt` | `LocalChitalkaLocale`, `LocalChitalkaThemeMode`, `LocalChitalkaThemeColors`. |

---

## Точка входа процесса

| Файл | Роль |
|------|------|
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ChitalkaApplication.kt` | `installConsoleCapture` (отладка). |
| `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/MainActivity.kt` | `setContent { ChitalkaApp(this) }`. |
| `chitalka-kotlin/app/src/main/AndroidManifest.xml` | Регистрация Application, Activity. |

---

## Ресурсы и тесты

| Категория | Путь |
|-----------|------|
| Ресурсы | `chitalka-kotlin/app/src/main/res/` |
| Инструментальный тест | `chitalka-kotlin/app/src/androidTest/java/com/ncorti/kotlin/template/app/MainActivityTest.kt` |

---

## Заметки по сопровождению

- Логика «что показывать в drawer» описана в `library-kotlin` (`DrawerNavigationSpec`); в `app` только отрисовка и роутинг по `DrawerScreen`.
- Любое новое подключение к БД или EPUB на уровне UI должно идти через типы из `library-android`, а строки/контракты экранов — из `library-kotlin`.
