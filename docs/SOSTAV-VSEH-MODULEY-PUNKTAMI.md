# Полный состав модулей и подмодулей (`chitalka-kotlin`)

Пути от корня репозитория **`chitalka-kotlin/`**. Исключены каталоги сборки (`build/`, `.gradle/`) и IDE (`.idea/`).

Связанные документы: [MODULI-I-KOMPONENTY.md](MODULI-I-KOMPONENTY.md), [moduli-detail/README.md](moduli-detail/README.md), [modules/README.md](modules/README.md).

**Описание каждого подмодуля** (расположение, назначение, связи): [submoduli/README.md](submoduli/README.md).

---

## Корень проекта (не отдельный Gradle-модуль приложения)

- `settings.gradle.kts` — список модулей `app`, `library-android`, `library-compose`, `library-kotlin`
- `build.gradle.kts` — корневой проект
- `gradle.properties` — версии, `APP_ID` и др.
- `gradle/libs.versions.toml` — каталог версий зависимостей
- `gradle/wrapper/gradle-wrapper.properties` — версия Gradle Wrapper
- `gradlew`, `gradlew.bat` — скрипты Wrapper
- `gradle/wrapper/gradle-wrapper.jar` — бинарник Wrapper
- `renovate.json` — конфиг Renovate
- `README.md`, `TROUBLESHOOTING.md`, `LICENSE`, `CHITALKA_ORIGIN.txt` — корневая документация
- `config/detekt/detekt.yml` — правила Detekt
- `.github/` — шаблоны issue/PR, workflows (CI)

---

## Подмодуль `buildSrc/` (логика Gradle, не в APK)

- `buildSrc/build.gradle.kts`
- `buildSrc/settings.gradle.kts`
- `buildSrc/src/main/kotlin/cleanup.gradle.kts`
- `buildSrc/src/main/kotlin/publish.gradle.kts`

---

## Модуль `app/` (Android Application)

### Корень модуля

- `app/build.gradle.kts`
- `app/proguard-rules.pro`

### Исходники Kotlin/Java — `app/src/main/java/com/ncorti/kotlin/template/app/`

- `ChitalkaApplication.kt`
- `MainActivity.kt`

### Подмодуль UI — `.../app/ui/`

- `ChitalkaApp.kt`
- `ChitalkaAppController.kt`
- `ChitalkaCompositionLocals.kt`
- `ChitalkaMainShell.kt`
- `ChitalkaDrawerRouter.kt`
- `ChitalkaNavHost.kt`
- `ChitalkaNavigationSetup.kt`
- `AppNavRoutes.kt`
- `ReaderRouteScreen.kt`
- `ReaderRouteUiModel.kt`

### Подмодуль UI — `.../app/ui/theme/`

- `ChitalkaTheme.kt`

### Подмодуль UI — `.../app/ui/reader/`

- `ChitalkaReaderScreen.kt`
- `ChitalkaReaderWebView.kt`
- `ReactNativeWebPolyfill.kt`

### Подмодуль UI — `.../app/ui/library/`

- `ChitalkaLibraryListPane.kt`
- `ChitalkaTrashPane.kt`

### Подмодуль UI — `.../app/ui/settings/`

- `ChitalkaSettingsPane.kt`

### Подмодуль UI — `.../app/ui/debug/`

- `ChitalkaDebugLogsPane.kt`

### Ресурсы — `app/src/main/`

- `AndroidManifest.xml`
- `res/layout/activity_main.xml`
- `res/values/strings.xml`
- `res/values/styles.xml`
- `res/values/colors.xml`
- `res/values/dimens.xml`
- `res/drawable/ic_launcher_background.xml`
- `res/drawable-v24/ic_launcher_foreground.xml`
- `res/mipmap-anydpi-v26/ic_launcher.xml`
- `res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `res/mipmap-hdpi/ic_launcher.png`, `ic_launcher_round.png`
- `res/mipmap-mdpi/ic_launcher.png`, `ic_launcher_round.png`
- `res/mipmap-xhdpi/ic_launcher.png`, `ic_launcher_round.png`
- `res/mipmap-xxhdpi/ic_launcher.png`, `ic_launcher_round.png`
- `res/mipmap-xxxhdpi/ic_launcher.png`, `ic_launcher_round.png`
- `res/xml/chitalka_file_paths.xml`

### Тесты — `app/src/androidTest/java/com/ncorti/kotlin/template/app/`

- `MainActivityTest.kt`

---

## Модуль `library-kotlin/` (JVM Library)

### Корень модуля

- `library-kotlin/build.gradle.kts`

### Подмодуль `com.chitalka.core.types` — `src/main/kotlin/com/chitalka/core/types/`

- `LibraryBookRecord.kt`
- `LibraryBookWithProgress.kt`
- `ReadingProgress.kt`

### Подмодуль `com.chitalka.library` — `.../library/`

- `LibrarySessionState.kt`
- `LibraryBookLookup.kt`
- `LastOpenBook.kt`
- `LastOpenReaderRestore.kt`

### Подмодуль `com.chitalka.navigation` — `.../navigation/`

- `NavTypes.kt`
- `RootStackDestination.kt`
- `DrawerNavigationSpec.kt`
- `ReaderRouteLifecycle.kt`

### Подмодуль `com.chitalka.screens.*` — `.../screens/`

- `readingnow/ReadingNowScreenSpec.kt`
- `books/BooksAndDocsScreenSpec.kt`
- `favorites/FavoritesScreenSpec.kt`
- `trash/TrashScreenSpec.kt`
- `settings/SettingsScreenSpec.kt`
- `debuglogs/DebugLogsScreenSpec.kt`
- `reader/ReaderScreenSpec.kt`
- `common/BookListScreenLayout.kt`
- `common/BookListSearchFilter.kt`

### Подмодуль `com.chitalka.ui.*` — `.../ui/`

- `bookcard/BookCardSpec.kt`
- `bookactions/BookActionsSheetSpec.kt`
- `topbar/AppTopBarSpec.kt`
- `firstlaunch/FirstLaunchModalSpec.kt`
- `readerview/ReaderBridgeScripts.kt`
- `readerview/ReaderBridgeMessages.kt`
- `readerview/ReaderDarkModeHtml.kt`
- `readerview/ReaderPageDirection.kt`

### Подмодуль `com.chitalka.i18n` — `.../i18n/`

- `I18nTypes.kt`
- `I18nCatalog.kt`
- `I18nPreferences.kt`

### Подмодуль `com.chitalka.theme` — `.../theme/`

- `ThemeColors.kt`
- `ThemePreferences.kt`

### Подмодуль `com.chitalka.picker` — `.../picker/`

- `EpubPickResult.kt`
- `EpubPickerUtils.kt`

### Подмодуль `com.chitalka.epub` — `.../epub/`

- `EpubErrorCodes.kt`

### Подмодуль `com.chitalka.debug` — `.../debug/`

- `DebugLog.kt`
- `InstallConsoleCapture.kt`
- `DebugAutoLoadEpub.kt`

### Подмодуль `com.chitalka.utils` — `.../utils/`

- `WithTimeout.kt`

### Ресурсы JVM — `library-kotlin/src/main/resources/`

- `chitalka/i18n/ru.json`
- `chitalka/i18n/en.json`
- `chitalka/reader/injectedScrollBridge.js`

### Шаблон (не домен Chitalka) — `src/main/java/com/ncorti/kotlin/template/library/`

- `FactorialCalculator.kt`

### Тесты — `library-kotlin/src/test/kotlin/com/chitalka/`

- `core/types/CoreTypesTest.kt`
- `screens/readingnow/ReadingNowScreenSpecTest.kt`
- `screens/books/BooksAndDocsScreenSpecTest.kt`
- `screens/favorites/FavoritesScreenSpecTest.kt`
- `screens/trash/TrashScreenSpecTest.kt`
- `screens/settings/SettingsScreenSpecTest.kt`
- `screens/debuglogs/DebugLogsScreenSpecTest.kt`
- `screens/reader/ReaderScreenSpecTest.kt`
- `navigation/NavTypesTest.kt`
- `navigation/RootStackDestinationTest.kt`
- `navigation/DrawerNavigationSpecTest.kt`
- `navigation/ReaderRouteLifecycleTest.kt`
- `library/LibrarySessionStateTest.kt`
- `library/LastOpenBookTest.kt`
- `library/LastOpenReaderRestoreTest.kt`
- `i18n/I18nTypesTest.kt`
- `i18n/I18nCatalogTest.kt`
- `i18n/I18nPreferencesTest.kt`
- `theme/ThemeColorsTest.kt`
- `theme/ThemePreferencesTest.kt`
- `picker/EpubPickerUtilsTest.kt`
- `ui/bookcard/BookCardSpecTest.kt`
- `ui/bookactions/BookActionsSheetSpecTest.kt`
- `ui/topbar/AppTopBarSpecTest.kt`
- `ui/firstlaunch/FirstLaunchModalSpecTest.kt`
- `ui/readerview/ReaderBridgeScriptsTest.kt`
- `ui/readerview/ReaderBridgeMessagesTest.kt`
- `ui/readerview/ReaderDarkModeHtmlTest.kt`
- `debug/DebugLogTest.kt`
- `debug/DebugAutoLoadEpubTest.kt`
- `debug/InstallConsoleCaptureTest.kt`
- `utils/WithTimeoutTest.kt`

### Тесты шаблона — `library-kotlin/src/test/java/com/ncorti/kotlin/template/library/`

- `FactorialCalculatorTest.kt`

---

## Модуль `library-android/` (Android Library)

### Корень модуля

- `library-android/build.gradle.kts`
- `library-android/consumer-rules.pro`
- `library-android/proguard-rules.pro`

### Подмодуль `com.chitalka.storage` — `src/main/java/com/chitalka/storage/`

- `StorageService.kt`
- `StorageServiceError.kt`
- `ChitalkaSqliteOpenHelper.kt`

### Подмодуль `com.chitalka.epub` — `.../epub/`

- `EpubService.kt`
- `EpubIo.kt`
- `EpubMetadata.kt`
- `EpubOpfXml.kt`
- `EpubUriUtils.kt`
- `EpubTypes.kt`

### Подмодуль `com.chitalka.library` — `.../library/`

- `ImportEpubToLibrary.kt`
- `LibrarySessionRefresh.kt`

### Подмодуль `com.chitalka.picker` — `.../picker/`

- `EpubPickerAndroid.kt`

### Подмодуль `com.chitalka.navigation` — `.../navigation/`

- `ReaderNavCoordinator.kt`

### Подмодуль `com.chitalka.prefs` — `.../prefs/`

- `SharedPreferencesKeyValueStore.kt`

### Подмодуль `com.chitalka.debug` — `.../debug/`

- `DebugAutoLoadEpubRunner.kt`
- `ChitalkaMirrorLog.kt`

### Шаблон — `.../com/ncorti/kotlin/template/library/android/`

- `ToastUtil.kt`

### Тесты — `library-android/src/androidTest/java/`

- `com/chitalka/storage/StorageServiceInstrumentedTest.kt`
- `com/ncorti/kotlin/template/library/android/ToastUtilTest.kt`

---

## Модуль `library-compose/` (Android Library, шаблон)

### Корень модуля

- `library-compose/build.gradle.kts`
- `library-compose/consumer-rules.pro`
- `library-compose/proguard-rules.pro`
- `library-compose/.gitignore`

### Подмодуль исходников — `library-compose/src/main/java/com/ncorti/kotlin/template/app/`

- `ComposeActivity.kt`

### Подмодуль UI — `.../app/ui/components/`

- `Factorial.kt`

### Манифест

- `library-compose/src/main/AndroidManifest.xml`

### Тесты

- `library-compose/src/androidTest/java/com/ncorti/kotlin/template/app/FactorialTest.kt`

---

## Документация в репозитории (не Gradle-модуль)

Каталог `chitalka-kotlin/docs/`:

- `MODULI-I-KOMPONENTY.md`
- `SOSTAV-VSEH-MODULEY-PUNKTAMI.md` (этот файл)
- `modules/README.md`, `modules/app.md`, `modules/library-kotlin.md`, `modules/library-android.md`, `modules/library-compose.md`
- `moduli-detail/README.md` и файлы `moduli-detail/sec-*.md`

---

*При добавлении файлов в модуль обновляйте этот список.*
