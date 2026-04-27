# Модули и части проекта Chitalka (Kotlin)

Пути указаны относительно корня репозитория: каталог `chitalka-kotlin/`.

**Подробная документация по каждому Gradle-подмодулю** (теги, зависимости, диаграммы связей, полные пути): каталог [docs/modules/README.md](modules/README.md).

**По каждому пункту оглавления ниже** — отдельный файл: [docs/moduli-detail/README.md](moduli-detail/README.md) (индекс `sec-*.md` ↔ §1–§6).

**Полный перечень файлов по всем модулям и подмодулям (пункты)** — [docs/SOSTAV-VSEH-MODULEY-PUNKTAMI.md](SOSTAV-VSEH-MODULEY-PUNKTAMI.md).

**Описание подмодулей** (где лежит, что делает, с чем связан): [docs/submoduli/README.md](submoduli/README.md).

---

## 1. Gradle-модули (состав проекта)

Документация: [sec-01-gradle-sostav.md](moduli-detail/sec-01-gradle-sostav.md).

Подключение в `chitalka-kotlin/settings.gradle.kts`:

| Модуль | Назначение |
|--------|------------|
| **app** | Android-приложение: Activity, Compose UI, навигация, интеграция читалки. |
| **library-kotlin** | Общая JVM/Kotlin-логика без Android: модели библиотеки, спецификации экранов, навигационные типы, мост к Web-читалке, тесты unit. |
| **library-android** | Android-реализация: SQLite/хранилище, EPUB (разбор, импорт), системный выбор файла, координатор навигации к читалке. |
| **library-compose** | Остаток шаблона (демо factorial + отдельная `ComposeActivity`); **не подключён** к `app` и не участвует в сборке основного приложения. |

Дополнительно: **`chitalka-kotlin/buildSrc/`** — скрипты Gradle (`cleanup`, `publish`), не runtime-модуль приложения.

---

## 2. Модуль `app` — приложение и UI

### 2.1. Запуск и жизненный цикл процесса

Документация: [sec-02-1-app-zapusk.md](moduli-detail/sec-02-1-app-zapusk.md).

| Часть | Путь |
|-------|------|
| Класс `Application` | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ChitalkaApplication.kt` |
| Главная Activity (точка входа UI) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/MainActivity.kt` |
| Манифест | `chitalka-kotlin/app/src/main/AndroidManifest.xml` |
| Классическая разметка для Activity (если используется) | `chitalka-kotlin/app/src/main/res/layout/activity_main.xml` |

### 2.2. Корневой Compose-слой и состояние приложения

Документация: [sec-02-2-app-compose-koren.md](moduli-detail/sec-02-2-app-compose-koren.md).

| Часть | Путь |
|-------|------|
| Корневой composable приложения | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaApp.kt` |
| Контроллер верхнего уровня (сессия библиотеки, побочные эффекты) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaAppController.kt` |
| Локальные провайдеры Composition | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaCompositionLocals.kt` |
| Тема Material / цвета приложения | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/theme/ChitalkaTheme.kt` |

### 2.3. Навигация (корневой граф, drawer, маршрут читалки)

Документация: [sec-02-3-app-navigatsiya.md](moduli-detail/sec-02-3-app-navigatsiya.md).

| Часть | Путь |
|-------|------|
| Настройка NavHost / связывание графа | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaNavigationSetup.kt` |
| Реализация NavHost (экраны стека) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaNavHost.kt` |
| Оболочка: drawer + область контента | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaMainShell.kt` |
| Drawer + роутинг разделов (пункты и контент) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ChitalkaMainShell.kt`, `ChitalkaDrawerRouter.kt` |
| Вспомогательные маршруты приложения (в т.ч. переход в читалку) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/AppNavRoutes.kt` |

### 2.4. Открытие книги и экран читалки (нативная оболочка вокруг Web)

Документация: [sec-02-4-app-chitalka.md](moduli-detail/sec-02-4-app-chitalka.md).

| Часть | Путь |
|-------|------|
| Экран маршрута «Читалка» (обвязка над WebView) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ReaderRouteScreen.kt` |
| UI-модель / состояние маршрута читалки | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/ReaderRouteUiModel.kt` |
| Экран читалки (Compose) | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ChitalkaReaderScreen.kt` |
| Встраивание WebView и загрузка контента | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ChitalkaReaderWebView.kt` |
| Полифиллы для совместимости с RN/Web-читалкой | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/reader/ReactNativeWebPolyfill.kt` |

### 2.5. Ресурсы и тесты приложения

Документация: [sec-02-5-app-resursy-testy.md](moduli-detail/sec-02-5-app-resursy-testy.md).

| Часть | Путь |
|-------|------|
| Строки, стили, цвета, размеры | `chitalka-kotlin/app/src/main/res/values/strings.xml`, `styles.xml`, `colors.xml`, `dimens.xml` |
| Иконки launcher | `chitalka-kotlin/app/src/main/res/mipmap-*`, `drawable-*`, `mipmap-anydpi-v26/` |
| Инструментальный тест Activity | `chitalka-kotlin/app/src/androidTest/java/com/ncorti/kotlin/template/app/MainActivityTest.kt` |

---

## 3. Модуль `library-kotlin` — домен, экраны, навигация, мост читалки

### 3.1. Типы данных библиотеки и прогресса чтения

Документация: [sec-03-1-library-kotlin-tipy.md](moduli-detail/sec-03-1-library-kotlin-tipy.md).

| Часть | Путь |
|-------|------|
| Запись книги в библиотеке | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/LibraryBookRecord.kt` |
| Книга с прогрессом чтения | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/LibraryBookWithProgress.kt` |
| Модель прогресса чтения | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/core/types/ReadingProgress.kt` |

### 3.2. Сессия библиотеки и восстановление последней открытой книги

Документация: [sec-03-2-library-kotlin-sessiya.md](moduli-detail/sec-03-2-library-kotlin-sessiya.md).

| Часть | Путь |
|-------|------|
| Состояние сессии библиотеки (списки, выбор, фильтры) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LibrarySessionState.kt` |
| Поиск книги по идентификатору | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LibraryBookLookup.kt` |
| Сохранение / чтение «последней открытой книги» | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LastOpenBook.kt` |
| Восстановление читалки при старте | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/library/LastOpenReaderRestore.kt` |

### 3.3. Навигация (типы маршрутов, drawer, жизненный цикл читалки)

Документация: [sec-03-3-library-kotlin-navigatsiya.md](moduli-detail/sec-03-3-library-kotlin-navigatsiya.md).

| Часть | Путь |
|-------|------|
| Имена корневых маршрутов и параметры | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/NavTypes.kt` |
| Экраны корневого стека (Main / Reader) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/RootStackDestination.kt` |
| Спецификация пунктов drawer | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/DrawerNavigationSpec.kt` |
| Жизненный цикл маршрута читалки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/navigation/ReaderRouteLifecycle.kt` |

### 3.4. Спецификации экранов библиотеки (контракт UI/данных)

Документация: [sec-03-4-library-kotlin-spetsifikatsii-ekranov.md](moduli-detail/sec-03-4-library-kotlin-spetsifikatsii-ekranov.md).

| Экран (смысл) | Основной файл |
|---------------|----------------|
| Сейчас читаю | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/readingnow/ReadingNowScreenSpec.kt` |
| Книги и документы | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/books/BooksAndDocsScreenSpec.kt` |
| Избранное | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/favorites/FavoritesScreenSpec.kt` |
| Корзина | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/trash/TrashScreenSpec.kt` |
| Настройки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/settings/SettingsScreenSpec.kt` |
| Отладочные логи (строки UI, экспорт, копирование) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/debuglogs/DebugLogsScreenSpec.kt` |
| Читалка (контракт экрана) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/reader/ReaderScreenSpec.kt` |
| Общая раскладка списка книг | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/common/BookListScreenLayout.kt` |
| Поиск и фильтр списка | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/screens/common/BookListSearchFilter.kt` |

Соответствующие unit-тесты лежат в `chitalka-kotlin/library-kotlin/src/test/kotlin/com/chitalka/screens/...` (те же пакеты, суффикс `*Test.kt`).

### 3.5. Карточка книги, действия, верхняя панель, первый запуск

Документация: [sec-03-5-library-kotlin-ui-spetsifikatsii.md](moduli-detail/sec-03-5-library-kotlin-ui-spetsifikatsii.md).

| Часть | Путь |
|-------|------|
| Спецификация карточки книги | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/bookcard/BookCardSpec.kt` |
| Нижний лист действий с книгой | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/bookactions/BookActionsSheetSpec.kt` |
| Спецификация верхней панели | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/topbar/AppTopBarSpec.kt` |
| Модальное окно первого запуска | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/firstlaunch/FirstLaunchModalSpec.kt` |

### 3.6. Мост нативного слоя к Web-читалке (скрипты, сообщения, тема страницы)

Документация: [sec-03-6-library-kotlin-most-chitalki.md](moduli-detail/sec-03-6-library-kotlin-most-chitalki.md).

| Часть | Путь |
|-------|------|
| JS-мост и вспомогательные скрипты | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderBridgeScripts.kt` |
| Сообщения между WebView и нативом | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderBridgeMessages.kt` |
| HTML/оформление тёмной темы для страницы читалки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderDarkModeHtml.kt` |
| Направление постраничной навигации (LTR/RTL) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderPageDirection.kt` |

### 3.7. Локализация и тема (предпочтения)

Документация: [sec-03-7-library-kotlin-i18n-tema.md](moduli-detail/sec-03-7-library-kotlin-i18n-tema.md).

| Часть | Путь |
|-------|------|
| Каталог строк / локализация | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/I18nCatalog.kt` |
| Типы локали | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/I18nTypes.kt` |
| Настройки языка | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/i18n/I18nPreferences.kt` |
| Палитра темы | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/theme/ThemeColors.kt` |
| Предпочтения темы | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/theme/ThemePreferences.kt` |

### 3.8. Выбор EPUB (чистая логика, без Activity)

Документация: [sec-03-8-library-kotlin-picker.md](moduli-detail/sec-03-8-library-kotlin-picker.md).

| Часть | Путь |
|-------|------|
| Результат выбора файла | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/picker/EpubPickResult.kt` |
| Утилиты разбора URI/путей выбора | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/picker/EpubPickerUtils.kt` |

### 3.9. EPUB — коды ошибок (общие)

Документация: [sec-03-9-library-kotlin-epub-oshibki.md](moduli-detail/sec-03-9-library-kotlin-epub-oshibki.md) — стабильные константы vs произвольные сообщения `EpubServiceError` в UI.

| Часть | Путь |
|-------|------|
| Коды ошибок EPUB | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/epub/EpubErrorCodes.kt` |

### 3.10. Отладка и вспомогательные утилиты

Документация: [sec-03-10-library-kotlin-otladka.md](moduli-detail/sec-03-10-library-kotlin-otladka.md).

| Часть | Путь |
|-------|------|
| Лог отладки | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/DebugLog.kt` |
| Перехват консоли (stdout/stderr → буфер) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/InstallConsoleCapture.kt` |
| Правила автозагрузки EPUB в debug | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/DebugAutoLoadEpub.kt` |
| Операции с таймаутом | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/utils/WithTimeout.kt` |

### 3.11. Наследие шаблона (не домен Chitalka)

Документация: [sec-03-11-library-kotlin-shablon.md](moduli-detail/sec-03-11-library-kotlin-shablon.md).

| Часть | Путь |
|-------|------|
| Демо «факториал» | `chitalka-kotlin/library-kotlin/src/main/java/com/ncorti/kotlin/template/library/FactorialCalculator.kt` |
| Тест к нему | `chitalka-kotlin/library-kotlin/src/test/java/com/ncorti/kotlin/template/library/FactorialCalculatorTest.kt` |

---

## 4. Модуль `library-android` — платформа Android

### 4.1. Хранилище и база

Документация: [sec-04-1-library-android-hranilishche.md](moduli-detail/sec-04-1-library-android-hranilishche.md).

| Часть | Путь |
|-------|------|
| Сервис хранилища (CRUD библиотеки, файлы) | `chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/StorageService.kt` |
| Ошибки хранилища | `chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/StorageServiceError.kt` |
| SQLite OpenHelper | `chitalka-kotlin/library-android/src/main/java/com/chitalka/storage/ChitalkaSqliteOpenHelper.kt` |
| Инструментальный тест хранилища | `chitalka-kotlin/library-android/src/androidTest/java/com/chitalka/storage/StorageServiceInstrumentedTest.kt` |

### 4.2. EPUB: разбор, метаданные, импорт в библиотеку

Документация: [sec-04-2-library-android-epub.md](moduli-detail/sec-04-2-library-android-epub.md) — там же ниже таблицы: шаги распаковки и `open()`, **`Dispatchers.IO`** для разбора OPF/spine, проброс **`EpubServiceError`** без маскировки, устойчивое чтение **`container.xml`/OPF** (UTF-8, BOM).

| Часть | Путь |
|-------|------|
| Сервис работы с EPUB | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubService.kt` |
| Ввод-вывод EPUB (ZIP, потоки) | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubIo.kt` |
| Метаданные (обложка, автор и т.д.) | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubMetadata.kt` |
| Разбор OPF/XML | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubOpfXml.kt` |
| Утилиты URI внутри EPUB | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubUriUtils.kt` |
| Типы данных EPUB (Android) | `chitalka-kotlin/library-android/src/main/java/com/chitalka/epub/EpubTypes.kt` |
| Импорт файла EPUB в библиотеку | `chitalka-kotlin/library-android/src/main/java/com/chitalka/library/ImportEpubToLibrary.kt` |

### 4.3. Системный выбор файла и навигация к читалке

Документация: [sec-04-3-library-android-picker-chitalka.md](moduli-detail/sec-04-3-library-android-picker-chitalka.md).

| Часть | Путь |
|-------|------|
| Android Activity Result API для выбора EPUB | `chitalka-kotlin/library-android/src/main/java/com/chitalka/picker/EpubPickerAndroid.kt` |
| Координация перехода в читалку по bookId/path | `chitalka-kotlin/library-android/src/main/java/com/chitalka/navigation/ReaderNavCoordinator.kt` |

### 4.4. Настройки и обновление сессии

Документация: [sec-04-4-library-android-prefs-sessiya.md](moduli-detail/sec-04-4-library-android-prefs-sessiya.md).

| Часть | Путь |
|-------|------|
| Key-value на SharedPreferences | `chitalka-kotlin/library-android/src/main/java/com/chitalka/prefs/SharedPreferencesKeyValueStore.kt` |
| Обновление сессии библиотеки после операций | `chitalka-kotlin/library-android/src/main/java/com/chitalka/library/LibrarySessionRefresh.kt` |

### 4.5. Отладка на устройстве

Документация: [sec-04-5-library-android-otladka.md](moduli-detail/sec-04-5-library-android-otladka.md).

| Часть | Путь |
|-------|------|
| Запуск автозагрузки EPUB (runner) | `chitalka-kotlin/library-android/src/main/java/com/chitalka/debug/DebugAutoLoadEpubRunner.kt` |
| Зеркало `android.util.Log` в буфер отладки | `chitalka-kotlin/library-android/src/main/java/com/chitalka/debug/ChitalkaMirrorLog.kt` |

### 4.6. Наследие шаблона

Документация: [sec-04-6-library-android-shablon.md](moduli-detail/sec-04-6-library-android-shablon.md).

| Часть | Путь |
|-------|------|
| Утилита Toast | `chitalka-kotlin/library-android/src/main/java/com/ncorti/kotlin/template/library/android/ToastUtil.kt` |
| Тест Toast | `chitalka-kotlin/library-android/src/androidTest/java/com/ncorti/kotlin/template/library/android/ToastUtilTest.kt` |

---

## 5. Модуль `library-compose` (не используется приложением)

Документация: [sec-05-library-compose.md](moduli-detail/sec-05-library-compose.md).

| Часть | Путь |
|-------|------|
| Отдельная Activity с демо Compose | `chitalka-kotlin/library-compose/src/main/java/com/ncorti/kotlin/template/app/ComposeActivity.kt` |
| Демо-компонент | `chitalka-kotlin/library-compose/src/main/java/com/ncorti/kotlin/template/app/ui/components/Factorial.kt` |
| Манифест библиотеки | `chitalka-kotlin/library-compose/src/main/AndroidManifest.xml` |
| Тест | `chitalka-kotlin/library-compose/src/androidTest/java/com/ncorti/kotlin/template/app/FactorialTest.kt` |

---

## 6. Сводка: «куда смотреть» для типовых задач

Документация: [sec-06-svodka-zadach.md](moduli-detail/sec-06-svodka-zadach.md).

| Задача | Где искать |
|--------|------------|
| Сменить корневой граф или экран читалки в стеке | `app/.../ChitalkaNavHost.kt`, `ChitalkaNavigationSetup.kt` |
| Пункты бокового меню и переключение разделов | `app/.../ChitalkaMainShell.kt`, `ChitalkaDrawerRouter.kt`, `library-kotlin/.../DrawerNavigationSpec.kt` |
| Открытие книги / URL маршрута Reader | `app/.../AppNavRoutes.kt`, `library-kotlin/.../NavTypes.kt`, `RootStackDestination.kt` |
| WebView и полифиллы читалки | `app/.../reader/ChitalkaReaderWebView.kt`, `ReactNativeWebPolyfill.kt` |
| JS-мост и сообщения читалки | `library-kotlin/.../readerview/ReaderBridge*.kt` |
| Импорт и разбор EPUB | `library-android/.../epub/`, `ImportEpubToLibrary.kt` |
| База и файлы библиотеки | `library-android/.../storage/StorageService.kt` |
| Логика списков и фильтров без Android | `library-kotlin/.../library/LibrarySessionState.kt`, `screens/common/` |
| Вкладка «Отладочные логи», копирование, источники строк | `app/.../ChitalkaDebugLogsPane.kt`, `library-kotlin/.../debug/DebugLog.kt`, `InstallConsoleCapture.kt`, `library-android/.../debug/ChitalkaMirrorLog.kt` |

Документ отражает состояние дерева исходников на момент создания; при добавлении файлов имеет смысл обновить соответствующий раздел.
