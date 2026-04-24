# Внутренние единицы модулей (микро-документация)

Каждый файл описывает **одну внутреннюю часть** родительского модуля из [`MODULES.md`](../../MODULES.md): контракт, поведение, связи. Родительские обзоры остаются в [`../01-entry-app-navigation.md`](../01-entry-app-navigation.md) … [`../04-i18n-theme-debug-config.md`](../04-i18n-theme-debug-config.md).

**Именование:** `{parent}-{nn}-{slug}.md` — `parent` совпадает с ID в MODULES (`entry`, `app-shell`, `storage`, …).

## Оглавление по родителю

### `entry` (`index.ts`)

| Файл | Внутренняя единица |
|------|-------------------|
| [entry-01-gesture-handler.md](./entry-01-gesture-handler.md) | Первый импорт `react-native-gesture-handler` |
| [entry-02-console-capture.md](./entry-02-console-capture.md) | Импорт `installConsoleCapture` и побочный эффект |
| [entry-03-register-root.md](./entry-03-register-root.md) | `registerRootComponent(App)` |

### `app-shell` (`App.tsx`)

| Файл | Внутренняя единица |
|------|-------------------|
| [app-shell-01-provider-tree.md](./app-shell-01-provider-tree.md) | `SafeAreaProvider` → Theme → I18n → `RootNavigator` |
| [app-shell-02-android-system-ui.md](./app-shell-02-android-system-ui.md) | `AndroidNavigationBar` + `StatusBar` |
| [app-shell-03-navigation-composition.md](./app-shell-03-navigation-composition.md) | `NavigationContainer`, `ref`, `onReady`, `LibraryProvider`, `RootStack` |

### `core-types`

| Файл | Внутренняя единица |
|------|-------------------|
| [core-type-reading-progress.md](./core-type-reading-progress.md) | Интерфейс `ReadingProgress` |
| [core-type-library-book-record.md](./core-type-library-book-record.md) | Интерфейс `LibraryBookRecord` |

### `util-timeout` / `util-android-copy` / `util-epub-picker`

| Файл | Внутренняя единица |
|------|-------------------|
| [util-timeout-with-timeout.md](./util-timeout-with-timeout.md) | Функция `withTimeout` |
| [util-android-copy-internal.md](./util-android-copy-internal.md) | `copyFileToInternalStorage` |
| [util-epub-picker-ids-and-filter.md](./util-epub-picker-ids-and-filter.md) | `deriveBookId`, `isEpubFileName`, проверка ассета |
| [util-epub-picker-pick-asset.md](./util-epub-picker-pick-asset.md) | `pickEpubAsset`, тип `EpubPickResult` |

### `storage`

| Файл | Внутренняя единица |
|------|-------------------|
| [storage-errors-and-logging.md](./storage-errors-and-logging.md) | `StorageServiceError`, `logError`, `wrapOpenFailure`, `wrapOperationFailure` |
| [storage-assertions.md](./storage-assertions.md) | `assertNonEmptyBookId`, `assertValidProgress` |
| [storage-open-migrate.md](./storage-open-migrate.md) | `getDatabase`, `openAndMigrate`, DDL |
| [storage-api-progress.md](./storage-api-progress.md) | `saveProgress`, `getProgress` |
| [storage-api-library.md](./storage-api-library.md) | `addBook`, `upsertLibraryBook`, `listLibraryBooks`, `getLibraryBook` |
| [storage-api-counts-clear.md](./storage-api-counts-clear.md) | `countLibraryBooks`, `countBooksWithProgress`, `clearAllData` |

### `epub-service`

| Файл | Внутренняя единица |
|------|-------------------|
| [epub-service-constants-and-log.md](./epub-service-constants-and-log.md) | Таймауты, строки ошибок, `logEpubOpen` |
| [epub-service-errors-and-html-escape.md](./epub-service-errors-and-html-escape.md) | `EpubServiceError`, `escapeHtmlAttrValue` |
| [epub-service-uri-path-helpers.md](./epub-service-uri-path-helpers.md) | `ensureFileUri`, каталоги, `fileUriToNativePath`, `joinUnderUnpackedRoot` |
| [epub-service-xml-opf-spine.md](./epub-service-xml-opf-spine.md) | Разбор OPF, spine, `readOpfFromUnpackedRoot`, `buildSpineFromOpfXml` |
| [epub-service-read-filesystem-metadata.md](./epub-service-read-filesystem-metadata.md) | `readFilesystemLibraryMetadata` |
| [epub-service-inject-viewport-css.md](./epub-service-inject-viewport-css.md) | `injectReaderViewportAndReflowCss` |
| [epub-service-resolve-chapter-assets.md](./epub-service-resolve-chapter-assets.md) | `resolveChapterAssetUri` (подготовка главы) |
| [epub-service-class-unpack.md](./epub-service-class-unpack.md) | `EpubService` конструктор, `getUnpackedRootUri`, `unpackThroughStep5` |
| [epub-service-class-open.md](./epub-service-class-open.md) | `open`, `getSpineChapterUri` |
| [epub-service-class-prepare-chapter.md](./epub-service-class-prepare-chapter.md) | `prepareChapter` / `prepareChapterBody` |
| [epub-service-class-metadata-cover-destroy.md](./epub-service-class-metadata-cover-destroy.md) | `getMetadata`, `resolveCoverFileUri`, `destroy` |

### `import-library`

| Файл | Внутренняя единица |
|------|-------------------|
| [import-library-helpers.md](./import-library-helpers.md) | `sanitizeFileStem`, `shortFileSuffix`, `coverExtensionFromUri`, `logImportStage` |
| [import-library-orchestration.md](./import-library-orchestration.md) | Тело `importEpubToLibrary` |

### `nav-types` / `nav-ref` / `nav-root-stack` / `nav-drawer` / `nav-reader-wrapper` + `ui-top-bar`

| Файл | Внутренняя единица |
|------|-------------------|
| [navigation-types-param-lists.md](./navigation-types-param-lists.md) | `DrawerParamList`, `RootStackParamList` |
| [navigation-ref-container-and-flush.md](./navigation-ref-container-and-flush.md) | `navigationRef`, `pendingReader`, `flushReaderNavigationIfPending` |
| [navigation-ref-navigate-to-reader.md](./navigation-ref-navigate-to-reader.md) | `navigateToReader`, цикл повторов |
| [navigation-root-stack.md](./navigation-root-stack.md) | `RootStack` |
| [navigation-app-drawer-shell.md](./navigation-app-drawer-shell.md) | `AppDrawer`: ширина, `screenOptions`, экраны |
| [navigation-app-drawer-placeholders.md](./navigation-app-drawer-placeholders.md) | Встроенные `ReadingNowScreen`, `FavoritesScreen`, … |
| [navigation-reader-wrapper.md](./navigation-reader-wrapper.md) | `ReaderScreenWrapper` |
| [navigation-app-top-bar.md](./navigation-app-top-bar.md) | `AppTopBar` |

### `library-context`

| Файл | Внутренняя единица |
|------|-------------------|
| [library-context-contract.md](./library-context-contract.md) | Тип значения контекста, `useLibrary` |
| [library-context-storage-epoch.md](./library-context-storage-epoch.md) | Экземпляр `StorageService`, `refreshBookCount`, `libraryEpoch`, `bumpLibraryEpoch` |
| [library-context-welcome-modal.md](./library-context-welcome-modal.md) | Видимость welcome, `suppressWelcomeForPicker`, `FirstLaunchModal` |
| [library-context-pick-toolbar.md](./library-context-pick-toolbar.md) | `pickEpubFromToolbar` |
| [library-context-pick-welcome.md](./library-context-pick-welcome.md) | `pickEpubFromWelcome`, задержка перед пикером |
| [library-context-open-books-search.md](./library-context-open-books-search.md) | `openBooksForSearch` |
| [library-context-debug-autoload-effect.md](./library-context-debug-autoload-effect.md) | `useEffect` с `runDebugAutoLoadEpubIfNeeded` |

### `ui-reader-view` / `ui-book-card` / `ui-first-launch`

| Файл | Внутренняя единица |
|------|-------------------|
| [ui-reader-view-webview-lifecycle.md](./ui-reader-view-webview-lifecycle.md) | `key={chapterKey}`, `source`, security props |
| [ui-reader-view-injected-scroll-bridge.md](./ui-reader-view-injected-scroll-bridge.md) | Строка `injectedScrollBridge` |
| [ui-reader-view-message-and-debounce.md](./ui-reader-view-message-and-debounce.md) | `onMessage`, debounce 350 ms |
| [ui-reader-view-initial-scroll.md](./ui-reader-view-initial-scroll.md) | `applyInitialScroll`, `onLoadEnd` |
| [ui-book-card.md](./ui-book-card.md) | Разметка и стили карточки |
| [ui-first-launch-modal.md](./ui-first-launch-modal.md) | Модалка первого запуска |

### Экраны (`screen-*`)

| Файл | Внутренняя единица |
|------|-------------------|
| [screen-reader-error-mapping.md](./screen-reader-error-mapping.md) | `clampChapterIndex`, `errorMessage` |
| [screen-reader-open-lifecycle.md](./screen-reader-open-lifecycle.md) | Эффект открытия книги, `epubRef`, `cancelled` |
| [screen-reader-progress-autosave.md](./screen-reader-progress-autosave.md) | `persistProgress`, `scheduleScrollSave`, таймеры |
| [screen-reader-chapter-navigation.md](./screen-reader-chapter-navigation.md) | `goChapter`, назад/вперёд |
| [screen-reader-render-phases.md](./screen-reader-render-phases.md) | `phase` loading/ready/error, разметка |
| [screen-books-and-docs.md](./screen-books-and-docs.md) | `BooksAndDocsScreen`: список, epoch, FAB |
| [screen-settings.md](./screen-settings.md) | `SettingsScreen` |
| [screen-debug-logs.md](./screen-debug-logs.md) | `DebugLogsScreen` |
| [screen-placeholder.md](./screen-placeholder.md) | `PlaceholderScreen` |
| [screen-library-legacy.md](./screen-library-legacy.md) | `LibraryScreen` (не в drawer) |

### `i18n-*`

| Файл | Внутренняя единица |
|------|-------------------|
| [i18n-types.md](./i18n-types.md) | `AppLocale`, ключи |
| [i18n-catalog.md](./i18n-catalog.md) | `catalog.ts`: `tSync`, `bookFallbackLabels` |
| [i18n-context-provider.md](./i18n-context-provider.md) | `I18nContext`, провайдер, `useI18n` |
| [i18n-locale-json.md](./i18n-locale-json.md) | `ru.json` / `en.json` как данные |
| [i18n-barrel.md](./i18n-barrel.md) | `src/i18n/index.ts` |

### `theme-*`

| Файл | Внутренняя единица |
|------|-------------------|
| [theme-colors.md](./theme-colors.md) | `colors.ts` |
| [theme-context.md](./theme-context.md) | `ThemeContext.tsx` |
| [theme-barrel.md](./theme-barrel.md) | `src/theme/index.ts` |

### `debug-*` и ассеты

| Файл | Внутренняя единица |
|------|-------------------|
| [debug-log-buffer.md](./debug-log-buffer.md) | `DebugLog.ts` |
| [debug-console-capture.md](./debug-console-capture.md) | `installConsoleCapture.ts` |
| [debug-autoload-epub.md](./debug-autoload-epub.md) | `debugAutoLoadEpub.ts` |
| [debug-epub-asset-types.md](./debug-epub-asset-types.md) | `epub-asset.d.ts` |
| [debug-bundled-epub-asset.md](./debug-bundled-epub-asset.md) | `assets/debug/ebook.demo.epub` |

### Конфигурация (`cfg-*`)

| Файл | Внутренняя единица |
|------|-------------------|
| [cfg-app-json.md](./cfg-app-json.md) | `app.json` |
| [cfg-package-json.md](./cfg-package-json.md) | `package.json` |
| [cfg-metro.md](./cfg-metro.md) | `metro.config.js` |
| [cfg-eas-json.md](./cfg-eas-json.md) | `eas.json` |
| [cfg-tsconfig.md](./cfg-tsconfig.md) | `tsconfig.json` |
| [cfg-script-android-licenses.md](./cfg-script-android-licenses.md) | `scripts/accept-android-licenses-and-sdk.ps1` |

---

При добавлении новой **внутренней** единицы: создайте файл по шаблону существующих (роль → входы/выходы → связанные файлы → риски для агентов) и строку в таблице выше.
