# Библиотека, контекст и экраны UI (для агентов)

Краткая карта: `LibraryProvider` / `useLibrary`, читалка (`ReaderScreen` + `ReaderView`), экраны drawer и вспомогательные компоненты.

---

## `LibraryContext` и `useLibrary`

**Файл:** `src/context/LibraryContext.tsx`.

### Публичный API (`LibraryContextValue`)

| Поле / метод | Назначение |
|----------------|------------|
| `bookCount: number` | Число книг в локальной библиотеке (после `StorageService.countLibraryBooks`). |
| `storageReady: boolean` | `true` после первой попытки обновить счётчик при монтировании провайдера. |
| `libraryEpoch: number` | Монотонно растёт после успешного импорта (и при `bumpLibraryEpoch`). Служит сигналом для списков: «данные библиотеки изменились». |
| `bumpLibraryEpoch()` | Вручную увеличить эпоху (например, импорт вне стандартного потока контекста). |
| `refreshBookCount()` | Перечитать количество книг из БД. |
| `pickEpubFromToolbar()` | Полный поток: пикер EPUB → `importEpubToLibrary` → инкремент эпохи, обновление счётчика → `navigateToReader(stableUri, bookId)`. Ошибки пикера — `Alert` с `t(messageKey)`; сбой импорта — сообщение или `library.importFailed`. |
| `openBooksForSearch()` | Если `navigationRef.isReady()`, навигация на `Main` с вложенным экраном `BooksAndDocs`. |

### `useLibrary()`

Хук обязан вызываться внутри `LibraryProvider`; иначе выбросится `Error('useLibrary must be used within LibraryProvider')`.

### Зачем `libraryEpoch`

Счётчик **не дублирует** `bookCount` по смыслу: список на `BooksAndDocsScreen` подписан на фокус (`useFocusEffect`) и дополнительно на `libraryEpoch`, чтобы после импорта из тулбара/приветственного модала список перезагрузился даже без повторного захода на экран. Любой код, который меняет библиотеку без прохождения через стандартный импорт в `LibraryProvider`, может вызвать `bumpLibraryEpoch()` + при необходимости `refreshBookCount()`.

### Приветственный модал (`FirstLaunchModal`) и `suppressWelcomeForPicker`

Условие видимости:

- `storageReady && bookCount === 0 && !welcomeDismissedSession && !suppressWelcomeForPicker`.

**Роль:** первый запуск при пустой библиотеке — текст из `firstLaunch.*`, кнопки «отмена» (закрыть на сессию) и «выбрать EPUB» (`onPickEpub` → `pickEpubFromWelcome`).

**`suppressWelcomeForPicker` (Android-особенность):** на Android системный document picker часто **не отображается поверх RN `Modal`**. Перед открытием пикера флаг ставится в `true`, модал скрывается; после завершения (в `finally`) — `false`. В `pickEpubFromWelcome` дополнительно пауза ~320 ms перед `pickEpubAsset`, чтобы модал успел сняться с экрана.

Тот же флаг используется в debug-автозагрузке EPUB: на время `runDebugAutoLoadEpubIfNeeded` модал подавляется, затем в `finally` снимается.

**Подсказки ошибок:** `welcomePickerHint` передаётся в `FirstLaunchModal` как `hint` (красный текст под сообщением); при ошибке импорта также может вызываться `Alert`.

---

## `ReaderScreen`: индекс главы, прогресс, ошибки

**Файл:** `src/screens/ReaderScreen.tsx`. В стеке обычно оборачивается в `ReaderScreenWrapper` (`src/navigation/ReaderScreenWrapper.tsx`), который передаёт `onBackToLibrary` / `onOpened` и дергает `refreshBookCount`.

### Состояние и фазы

- `phase`: `'loading' | 'ready' | 'error'`.
- `chapterIndex` — индекс в `spine` (0-based), ограничивается `clampChapterIndex`.
- При смене книги/пути эффект сбрасывает spine, пересоздаёт `EpubService`, читает `storage.getProgress(bookId)`, открывает EPUB, выбирает сохранённую главу и `scrollOffset`, готовит HTML главы.

### Сохранение прогресса

- `persistProgress(index, scrollY)` → `storage.saveProgress({ bookId, lastChapterIndex, scrollOffset, lastReadTimestamp })`; ошибки глотаются, чтение не блокируется.
- При скролле: `ReaderView` шлёт offset → `onScrollOffsetChange` → `scheduleScrollSave` с debounce **500 ms** (последнее значение в `latestScrollRef`).
- Перед переходом на другую главу (`goChapter`): синхронный вызов `persistProgress` для текущей главы и текущего скролла; новая глава стартует с `initialScrollY = 0` и сразу пишется прогресс для нового индекса.
- После первого успешного открытия книги дополнительно сохраняется прогресс и вызывается опциональный `onOpened`.

### Отображение ошибок и i18n

Функция `errorMessage(error, t)`:

- `EpubServiceError` с известными `message`: `EPUB_EMPTY_SPINE`, `EPUB_ERR_TIMEOUT_COPY`, `EPUB_ERR_TIMEOUT_UNZIP`, `EPUB_ERR_TIMEOUT_PREPARE_CHAPTER` → ключи `reader.errors.emptySpine`, `timeoutCopy`, `timeoutUnzip`, `timeoutPrepareChapter`.
- Иной `EpubServiceError` → `error.message` если непустой, иначе `reader.errors.openFailed`.
- Обычный `Error` → `error.message`.
- Иначе → `reader.errors.unknown`.

В UI фазы ошибки: заголовок `reader.errorTitle`, тело — результат `errorMessage`, опционально кнопка «к библиотеке» при наличии `onBackToLibrary`.

---

## `ReaderView`: контракт сообщений WebView

**Файл:** `src/components/ReaderView.tsx`.

### Входящие от WebView (`onMessage`)

Ожидается **строка с JSON**. После `JSON.parse`:

- Допустимый payload: `{ "t": "scroll", "y": number }`, где `y` конечное число.
- Иначе сообщение игнорируется; при невалидном JSON — тихий `catch`.

После валидного `scroll` вызывается `onScrollOffsetChange(y)` с **дополнительным debounce 350 ms** на стороне RN (внутри scroll в странице — throttling ~200 ms перед `postMessage`).

### Исходящее в страницу

Инжектится IIFE: на `scroll` (passive) вешается обработчик, который через таймер вызывает `postMessage(JSON.stringify({ t: 'scroll', y }))`, где `y` — `pageYOffset` / `scrollTop`.

### Прочее

- `key={chapterKey}` на `WebView` — полный сброс при смене главы.
- `onLoadEnd` → `injectJavaScript` с `window.scrollTo(0, initialScrollY)`.
- `source={{ html, baseUrl }}`; флаги доступа к файлам включены для локальных ресурсов EPUB.

---

## Компоненты (не экраны навигации)

### `BookCard`

Презентационная карточка: обложка или эмодзи-заглушка, заголовок, автор, размер в МБ + `t('common.mb')`. Зависимости: `useTheme`, `useI18n`, колбэк `onPress`.

### `FirstLaunchModal`

Полноэкранный полупрозрачный `Modal`: текст приветствия, опциональный `hint`, кнопки отмены и выбора файла. Сам по себе не знает про пикер — логика видимости и Android в `LibraryProvider`.

---

## Эраны: роль, действия, зависимости

### `BooksAndDocsScreen`

- **Роль:** основной список книг библиотеки (drawer «Книги» / `BooksAndDocs`).
- **Действия:** `FlatList` + `BookCard` → открытие `navigateToReader`; FAB «+» → `pickEpubFromToolbar` из `useLibrary`.
- **Зависимости:** `useLibrary` (`pickEpubFromToolbar`, `libraryEpoch`), `StorageService.listLibraryBooks`, `useFocusEffect` для перезагрузки при фокусе, `useTheme`, `useI18n`, safe area.

### `ReaderScreen` (через wrapper)

- **Роль:** чтение одной книги по `bookPath` / `bookId`: навигация по spine, WebView, автосохранение.
- **Действия:** «Назад к библиотеке» (если передан колбэк), предыдущая/следующая глава, скролл.
- **Зависимости:** `EpubService`, `StorageService`, `ReaderView`, `expo-file-system/legacy` для проверки `baseUrl`, `useI18n`.

### `SettingsScreen`

- **Роль:** настройки приложения.
- **Действия:** переключение темы (`light` / `dark`), языка (`APP_LOCALES`), отображение версии из `expo-constants`.
- **Зависимости:** `useTheme`, `useI18n`.

### `DebugLogsScreen`

- **Роль:** просмотр in-memory отладочных логов (`debug/DebugLog`).
- **Действия:** очистка, экспорт в файл в cache + `Sharing.shareAsync` (или путь в Alert, если шаринг недоступен).
- **Зависимости:** `debugLogSubscribe`, `expo-file-system/legacy`, `expo-sharing`, `useTheme`, `useI18n`, safe area.

### `PlaceholderScreen`

- **Роль:** заглушка с заголовком и опциональным подзаголовком.
- **Действия:** нет (только отображение).
- **Зависимости:** `useTheme`. Используется в `AppDrawer` для пунктов «Сейчас читаю», «Избранное», «Авторы», «Подборки», «Корзина» — отдельные локальные обёртки передают `t('screens.*')`.

### `LibraryScreen`

- **Роль:** упрощённый экран «выбери EPUB» с колбэком `onBookSelected(uri, bookId)` — **импорт в библиотеку не выполняется внутри экрана**; родитель должен обработать файлы. После успеха вызываются `bumpLibraryEpoch` и `refreshBookCount` из `useLibrary`.
- **Действия:** одна кнопка выбора файла, локальный `hint` при ошибке пикера.
- **Зависимости:** `useLibrary`, `useI18n`, `pickEpubAsset`.

**Важно для агентов:** `LibraryScreen` **нигде не подключён** к `AppDrawer` и к `RootStack` — в репозитории нет импорта этого экрана в навигацию. Актуальный пользовательский поток библиотеки — `BooksAndDocsScreen` + `LibraryProvider` (тулбар/приветствие). `LibraryScreen` пригоден как переиспользуемый модуль или для будущей вставки (см. также `docs/MODULES.md`).

---

## Связь навигации (кратко)

- `RootStack`: `Main` → `AppDrawer`; отдельный экран `Reader` → `ReaderScreenWrapper` → `ReaderScreen`.
- `LibraryProvider` оборачивает приложение выше навигации и рендерит `FirstLaunchModal` поверх `children`.
