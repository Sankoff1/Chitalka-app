# i18n, тема, отладка и конфигурация сборки

Краткая карта для агентов: где менять строки и тему, как устроены логи и автозагрузка демо-EPUB, какие файлы трогать при сборках.

## Локализация (`src/i18n/`)

### Типы и локали

- `AppLocale`: `'ru' | 'en'` (`types.ts`). Константа `APP_LOCALES`, ключ AsyncStorage: `LOCALE_STORAGE_KEY` (`chitalka_locale`).
- Новая локаль потребует правок в `types.ts`, `catalog.ts`, нового JSON в `locales/` и логики в `I18nContext` (загрузка/сохранение допустимых значений).

### Каталоги и ключи

- Переводы лежат в **`src/i18n/locales/ru.json`** и **`src/i18n/locales/en.json`**. При добавлении строки **обновляйте оба файла**, иначе в одной из локалей будет отображаться сырой ключ.
- В `catalog.ts` путь — **точечная нотация** (`drawer.settings`, `book.untitled`). В JSON это вложенные объекты; `getNested` обходит цепочку ключей.
- Плейсхолдеры в строках: `{{name}}` в тексте; подстановка через второй аргумент `vars: Record<string, string | number>` (регулярка `\{\{(\w+)\}\}`).

### `t` и `tSync`

- **`tSync(locale, path, vars?)`** — синхронный перевод **вне React** по текущей или переданной локали. Если в выбранной локали нет ключа, берётся **fallback на `ru`**, затем сам `path`.
- **`t(path, vars?)` из `useI18n()`** — обёртка над `tSync` с **текущей локалью из контекста** (`I18nContext`). По сути то же поведение, но реактивно при смене `locale`.
- Для подписей книг без контекста используется **`bookFallbackLabels(locale)`** (внутри — `tSync`).

### Провайдер

- `I18nProvider`: старт с `'ru'`, затем чтение AsyncStorage; `setLocale` пишет в storage. Компоненты вне провайдера не могут вызывать `useI18n()`.

## Тема (`src/theme/`)

### Режим и цвета

- **`ThemeMode`**: `'light' | 'dark'` (`colors.ts`).
- Палитры: `lightThemeColors`, `darkThemeColors`, переключение через **`getColorsForMode(mode)`**.
- **`ThemeProvider`**: стартовый `mode` из пропа **`initialMode`** (по умолчанию `'light'`); после монтирования **`useEffect`** читает AsyncStorage (`chitalka_theme_mode`) и применяет только **`'light'`** / **`'dark'`**. **`setMode`** и **`toggleTheme`** пишут в storage. Подробности — [`internals/theme-context.md`](./internals/theme-context.md).
- **`colors`**: **`getColorsForMode(mode)`** — ссылки на палитры в `colors.ts` статичны; отдельный `useMemo` в провайдере только для объекта значения контекста, не для вычисления `colors`.
- **`useTheme()`** возвращает `{ mode, colors, setMode, toggleTheme }`. Стили экранов и компонентов обычно берут **`colors`** из этого хука, а не импортируют константы напрямую (если не нужен статический снимок).
- **UI темы в настройках:** экран **`SettingsScreen`** — переключатель **`Switch`** (см. [`internals/screen-settings.md`](./internals/screen-settings.md)).

## Отладка (`src/debug/`)

### Буфер `DebugLog`

- **`MAX_ENTRIES = 4000`**. При превышении массив **обрезается с конца** (`slice(-MAX_ENTRIES)`), старые записи отбрасываются.
- API: `debugLogAppend`, `debugLogSubscribe`, `debugLogGetSnapshot`, `debugLogClear`, `debugLogFormatExport`.
- Уровни: `'log' | 'warn' | 'error' | 'debug' | 'info'`.

### Перехват `console` (`installConsoleCapture.ts`)

- **`installConsoleCapture()`** оборачивает `console.log/info/warn/error/debug`: сначала append в буфер, затем вызов **оригинального** метода.
- **Идемпотентность**: флаг на `globalThis` под ключом `__CHITALKA_CONSOLE_CAPTURE__`. Повторный импорт модуля **не дублирует** обёртки.
- В конце файла вызывается **`installConsoleCapture()`** при загрузке модуля — подключение side-effect’ом.

### Автозагрузка демо-EPUB (`debugAutoLoadEpub.ts`)

- **`DEBUG_AUTO_LOAD_EPUB_ENABLED`** (`true`/`false` в коде) — главный выключатель без правок `LibraryContext`.
- Активно только если **`__DEV__`**, флаг включён и **`Platform.OS` — `android` или `ios`** (не web).
- **`runDebugAutoLoadEpubIfNeeded`**: ассет `assets/debug/ebook.demo.epub` через `expo-asset`, идемпотентный импорт в библиотеку с фиксированным **`DEBUG_DEMO_BOOK_ID`**. Читалку **сама не открывает** — открытие управляется отдельным эффектом автооткрытия последней книги (см. `src/library/lastOpenBook.ts`): если перед закрытием приложения пользователь был в читалке, книга откроется; если в меню — остаётся `ReadingNow`.
- **Когда выполняется**: в **`LibraryProvider`** (`LibraryContext.tsx`) в `useEffect`, зависящем от `storageReady`, `locale`, `storage`, `refreshBookCount`. Условия: `storageReady`, активная автозагрузка по флагам/платформе, **`debugAutoLoadStarted` ref** — эффект **один раз за жизнь провайдера**. На время прогона подавляется welcome/picker (`suppressWelcomeForPicker`).

### Типы ассета EPUB

- **`epub-asset.d.ts`**: `declare module '*.epub'` — default export как **числовой id модуля** (как у Expo asset), для `import ... from '...epub'`.

## Конфигурация для сборок и инструментов

### `app.json`

- Корневой **Expo config**: имя, slug, **version**, `userInterfaceStyle`, **иконки/splash**, **android.package**, `blockedPermissions`, adaptive icon, **plugins** (`expo-sqlite`, `expo-font`, `expo-build-properties` с min/compile/target SDK, `expo-asset`), **`extra.eas.projectId`** для EAS.

### `eas.json`

- Профили **`development`** (dev client, internal), **`preview`** (internal, Android **APK**), **`production`** (**`autoIncrement`**). Версия приложения: **`appVersionSource: "remote"`** — согласованность с EAS.
- Меняйте при настройке CI, каналов дистрибуции или политики версий.

### `metro.config.js`

- База: `getDefaultConfig` из `expo/metro-config`.
- В **`resolver.assetExts`** добавлены **`wasm`** (web + expo-sqlite) и **`epub`** (бандл демо-книги). Без этого Metro не отдаст файлы как ассеты.

### `tsconfig.json`

- Расширяет **`expo/tsconfig.base`**, **`strict: true`**, **`resolveJsonModule: true`** (импорт `en.json`/`ru.json` в `catalog.ts`).

### `scripts/accept-android-licenses-and-sdk.ps1`

- **Windows / локальная машина**: неинтерактивное **`sdkmanager --licenses`**, установка **platform-tools**, **API 35**, **build-tools 35.0.0**; выставляет `JAVA_HOME`, `ANDROID_HOME` под `%LOCALAPPDATA%\Android\...`. Нужен для нативных/Android-сборок без ручного кликанья по лицензиям.

---

При добавлении строк: **оба JSON**, ключи с **точками** в коде = вложенность в JSON; в UI — **`t` из `useI18n`**, вне React — **`tSync`**. Тема — **`useTheme().colors`**, **`mode`**, **`setMode`/`toggleTheme`**; выбранный режим **персистится** в AsyncStorage (`chitalka_theme_mode`, см. [`internals/theme-context.md`](./internals/theme-context.md)); на экране настроек — **`Switch`** (см. [`internals/screen-settings.md`](./internals/screen-settings.md)). Логи — лимит **4000**, консоль — **один раз**. Демо-EPUB — только **dev + Android/iOS + флаг**, триггер в **`LibraryContext` после `storageReady`**. Сборки: **`app.json` / `eas.json` / `metro.config.js`**, нативный Android — скрипт лицензий/SDK при необходимости.
