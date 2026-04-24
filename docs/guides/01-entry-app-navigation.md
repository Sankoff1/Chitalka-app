# Точка входа, провайдеры и навигация

Документ для ИИ-агентов: порядок загрузки приложения, дерево провайдеров, навигационный граф, передача параметров в читалку и поведение верхней панели.

---

## 1. Порядок bootstrap и побочные эффекты

### `index.ts` (корень репозитория)

Выполняется **до** регистрации корневого компонента Expo, в строгом порядке импорта:

1. **`import 'react-native-gesture-handler'`** — обязательно первым в entry (требование библиотеки жестов для корректной работы drawer/stack и жестов навигации).
2. **`import './src/debug/installConsoleCapture'`** — при загрузке модуля вызывается **`installConsoleCapture()`** в конце файла: оборачиваются `console.log/info/warn/error/debug`, сообщения дублируются в буфер отладочных логов (`debugLogAppend`). Повторный импорт идемпотентен (флаг на `globalThis`).
3. **`registerRootComponent(App)`** — регистрирует `App` из `./App` как корневой компонент (`AppRegistry` / окружение Expo).

### `App.tsx`

После монтирования `App` дерево провайдеров (снаружи → внутрь):

| Уровень | Компонент | Назначение |
|---------|-----------|------------|
| 1 | `SafeAreaProvider` | инсеты safe area для потомков |
| 2 | `ThemeProvider` | тема (`useTheme`: `mode`, `colors`, …) |
| 3 | `I18nProvider` | локализация (`useI18n`) |
| 4 | `RootNavigator` | связка статус-бара, Android navigation bar, **`NavigationContainer`**, библиотека |

Внутри `RootNavigator`:

- `View` с фоном `colors.background`.
- `AndroidNavigationBar` — только Android: прозрачный фон панели навигации, overlay-swipe; стиль кнопок (светлые/тёмные) синхронизирован с `mode` темы.
- `StatusBar` из `expo-status-bar` — стиль контраста к теме.
- **`NavigationContainer`** с `ref={navigationRef}` и **`onReady={flushReaderNavigationIfPending}`** — критично для отложенных переходов на `Reader` (см. раздел про `navigationRef.ts`).
- **`LibraryProvider`** обёрнут **внутри** `NavigationContainer` (не снаружи): контекст библиотеки и модалка первого запуска живут под готовым контейнером навигации; методы вроде `openBooksForSearch` используют `navigationRef`, который привязан к этому же контейнеру.

Итог для агентов: менять порядок импортов в `index.ts` (особенно gesture-handler) или выносить `LibraryProvider` выше `NavigationContainer` без пересмотра `navigationRef` / `onReady` нельзя без регрессий.

---

## 2. Пофайлово

### `index.ts`

| | |
|--|--|
| **Назначение** | Единственная точка входа JS: жесты, захват консоли, регистрация корня. |
| **Публичный API** | Нет экспортов; побочные эффекты только через импорты и `registerRootComponent`. |
| **Ключевое поведение** | Gesture handler → консоль → Expo root. |
| **Зависимости внутрь** | `react-native-gesture-handler`, `./src/debug/installConsoleCapture`, `expo`, `./App`. |
| **Наружу** | Среда выполнения React Native / Expo. |
| **Подводные камни** | Без первого импорта gesture-handler навигация с жестами может вести себя некорректно. |

---

### `App.tsx`

| | |
|--|--|
| **Назначение** | Сборка глобальных провайдеров и корневого навигатора с темой и системными барами. |
| **Публичный API** | `export default function App()` — корневой компонент. |
| **Ключевое поведение** | Вложенность провайдеров; `NavigationContainer.onReady` сбрасывает отложенный переход на `Reader`; `LibraryProvider` внутри контейнера. |
| **Зависимости внутрь** | `@react-navigation/native`, `expo-navigation-bar`, `expo-status-bar`, `react-native-safe-area-context`, `./src/context/LibraryContext`, `./src/navigation/*`, `./src/i18n`, `./src/theme`. |
| **Наружу** | Регистрируется из `index.ts`. |
| **Подводные камни** | `flushReaderNavigationIfPending` должен оставаться колбэком `onReady`; без него ранний `navigateToReader` может потеряться. `useTheme` в `RootNavigator` требует обёртки `ThemeProvider` выше. |

---

### `src/navigation/types.ts`

| | |
|--|--|
| **Назначение** | Централизованные типы параметров экранов для type-safe навигации. |
| **Публичный API** | `DrawerParamList`, `RootStackParamList`. |
| **Ключевое поведение** | `Main` использует `NavigatorScreenParams<DrawerParamList> \| undefined` — вложенная навигация к drawer и опционально без параметров. `Reader` — строго `{ bookPath: string; bookId: string }`. |
| **Зависимости внутрь** | `@react-navigation/native` (`NavigatorScreenParams`). |
| **Наружу** | `navigationRef.ts`, `RootStack.tsx`, `ReaderScreenWrapper.tsx`, любые вызовы `navigate` с дженериком ref/stack. |
| **Подводные камни** | При добавлении экранов обновлять типы и все `navigate`/`createXNavigator` дженерики согласованно. |

---

### `src/navigation/navigationRef.ts`

| | |
|--|--|
| **Назначение** | Императивная навигация без хуков: ref на корневой контейнер, очередь перехода на `Reader`, устойчивость к гонке «navigate до ready». |
| **Публичный API** | `navigationRef`, `flushReaderNavigationIfPending()`, `navigateToReader(bookPath, bookId)`. |
| **Ключевое поведение** | `createNavigationContainerRef<RootStackParamList>()`. Перед переходом на `Reader` параметры кладутся в `pendingReader`; `flushReaderNavigationIfPending` при `isReady()` делает `navigate('Reader', p)` и сбрасывает очередь. `navigateToReader` сначала пытается flush; если контейнер ещё не готов — до 50 попыток с интервалом 50 ms, иначе предупреждение в консоль и сброс `pendingReader`. |
| **Зависимости внутрь** | `@react-navigation/native`, `./types`. |
| **Наружу** | `App.tsx` (`ref`, `onReady`), `LibraryContext` (`navigateToReader`, `openBooksForSearch`), другие вызовы императивной навигации. |
| **Подводные камни** | **`navigateToReader` без готового контейнера** полагается на `onReady` + таймеры; если убрать `onReady` или ref с контейнера, переход из импорта/пикера может не произойти. **`openBooksForSearch`** при `!navigationRef.isReady()` просто выходит — молчаливый no-op. Дублирующие контейнеры с одним ref не создавать. |

---

### `src/navigation/RootStack.tsx`

| | |
|--|--|
| **Назначение** | Корневой нативный stack: библиотека (drawer) и полноэкранный читатель. |
| **Публичный API** | `export function RootStack()`. |
| **Ключевое поведение** | `createNativeStackNavigator<RootStackParamList>`; экраны `Main` → `AppDrawer`, `Reader` → `ReaderScreenWrapper`; `headerShown: false` для обоих. |
| **Зависимости внутрь** | `@react-navigation/native-stack`, `./AppDrawer`, `./ReaderScreenWrapper`, `./types`. |
| **Наружу** | Рендерится как ребёнок `LibraryProvider` внутри `NavigationContainer` в `App.tsx`. |
| **Подводные камни** | Имена `'Main'` и `'Reader'` должны совпадать с `RootStackParamList` и с вызовами `navigationRef.navigate`. |

---

### `src/navigation/AppDrawer.tsx`

| | |
|--|--|
| **Назначение** | Drawer-навигатор по разделам приложения; общий кастомный header. |
| **Публичный API** | `export function AppDrawer()`. |
| **Ключевое поведение** | `createDrawerNavigator<DrawerParamList>`; ширина ящика `min(288, windowWidth - 24)`; `screenOptions.header` = `AppTopBar`; цвета из `useTheme`, подписи из `useI18n`. Экраны: ReadingNow, BooksAndDocs, Favorites, Authors, Collections, Cart, DebugLogs, Settings (часть — плейсхолдеры). |
| **Зависимости внутрь** | `@react-navigation/drawer`, экраны из `../screens/*`, `../components/AppTopBar`, `../i18n`, `../theme`, `./types`. |
| **Наружу** | Единственный child-экран `Main` в `RootStack`. |
| **Подводные камни** | Локальные обёртки экранов (ReadingNow и т.д.) объявлены в файле; при смене маршрутов синхронизировать `DrawerParamList` и список `Drawer.Screen`. |

---

### `src/navigation/ReaderScreenWrapper.tsx`

| | |
|--|--|
| **Назначение** | Адаптер стека `Reader`: достаёт `bookPath`/`bookId` из `route.params`, связывает с `LibraryContext` и `ReaderScreen`. |
| **Публичный API** | `export function ReaderScreenWrapper({ route, navigation }: Props)`. |
| **Ключевое поведение** | `NativeStackScreenProps<RootStackParamList, 'Reader'>`; прокидывает `bookPath`, `bookId` в `ReaderScreen`; `onBackToLibrary` вызывает `refreshBookCount()` и `navigation.goBack()`; `onOpened` — только `refreshBookCount()`. |
| **Зависимости внутрь** | `@react-navigation/native-stack`, `../context/LibraryContext`, `../screens/ReaderScreen`, `./types`. |
| **Наружу** | Регистрация экрана `Reader` в `RootStack`. |
| **Подводные камни** | Параметры маршрута обязательны по типам; открытие без `bookPath`/`bookId` — ошибка контракта навигации. |

---

### `src/components/AppTopBar.tsx`

| | |
|--|--|
| **Назначение** | Верхняя панель для drawer-экранов: меню, заголовок, опционально поиск. |
| **Публичный API** | `export function AppTopBar(props: DrawerHeaderProps)`. |
| **Ключевое поведение** | Заголовок из `options.title` (строка непустая). Кнопка меню: `navigation.openDrawer()`. |
| **Зависимости внутрь** | `@expo/vector-icons/MaterialIcons`, `@react-navigation/drawer`, `../context/LibraryContext`, `../i18n`, `../theme`, `react-native-safe-area-context`. |
| **Наружу** | Используется в `AppDrawer` как `header`. |
| **Подводные камни** | См. §4. |

---

## 3. Типы параметров навигации и путь `bookPath` / `bookId` в читалку

### Типы

- **`DrawerParamList`**: все ключи drawer — `undefined` (без параметров маршрута).
- **`RootStackParamList`**:
  - **`Main`**: вложенные параметры drawer (`NavigatorScreenParams<DrawerParamList>`) или `undefined`.
  - **`Reader`**: `{ bookPath: string; bookId: string }`.

### Как Reader получает данные

1. Императивно: **`navigateToReader(bookPath, bookId)`** в `navigationRef.ts` в итоге выполняет **`navigationRef.navigate('Reader', { bookPath, bookId })`** (сразу или после готовности контейнера через `pendingReader` + `flushReaderNavigationIfPending`).
2. Типобезопасность ref привязана к **`RootStackParamList`**, поэтому второй аргумент `navigate('Reader', …)` должен соответствовать полям `bookPath` и `bookId`.
3. **`ReaderScreenWrapper`** читает **`route.params`** и передаёт их в **`ReaderScreen`** как пропсы **`bookPath`** и **`bookId`**.

Связка с библиотекой: после импорта EPUB `LibraryContext` получает стабильный URI и `bookId` из `importEpubToLibrary` и вызывает `openReader` → внутри него **`navigateToReader(uri, bookId)`** (в коде параметр назван `stableUri` / `uri` — семантически это путь/URI к файлу книги в хранилище приложения).

---

## 4. AppTopBar: поиск и `openBooksForSearch`

- **Когда показывается иконка поиска**: **`bookCount > 0`** из **`useLibrary()`** (`const showSearch = bookCount > 0`). Пустая библиотека — слот справа пустой (не ренерится кнопка).
- **По нажатию**: вызывается **`openBooksForSearch`** из контекста **без аргументов** (передаётся как `onPress={openBooksForSearch}`).
- **Реализация в `LibraryContext`**: если **`!navigationRef.isReady()`** — **немедленный return** (ничего не происходит). Иначе **`navigationRef.navigate('Main', { screen: 'BooksAndDocs' })`** — переход к корню стека `Main` и вложенному экрану drawer **`BooksAndDocs`** (список книг/документов для поиска и работы с библиотекой).

Для агентов: при багах «поиск не открывается» проверять готовность навигации, ненулевой `bookCount` и то, что имя вложенного экрана всё ещё **`BooksAndDocs`** и совпадает с `DrawerParamList`.

---

## Краткая карта вызовов

`index.ts` → `App` → `NavigationContainer` + `onReady` / `navigationRef` → `LibraryProvider` → `RootStack` → (`Main`: `AppDrawer` + `AppTopBar`) | (`Reader`: `ReaderScreenWrapper` → `ReaderScreen`).
