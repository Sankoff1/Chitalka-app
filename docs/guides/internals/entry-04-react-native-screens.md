# Внутренняя единица: `react-native-screens` в entry

**Родительский модуль:** `entry` — `index.ts`  
**Файл кода:** `index.ts` (после `installConsoleCapture`, до `expo/registerRootComponent`)

## Назначение

Импорт **`enableScreens`** и **`enableFreeze`** из `react-native-screens` и вызов **`enableScreens(true)`**, **`enableFreeze(true)`** до регистрации корня — нативные контейнеры экранов и заморозка неактивных стеков (меньше работы reconciliation при переключении маршрутов).

## Порядок

Выполняется **после** перехвата консоли и **до** `registerRootComponent`, чтобы навигатор монтировался уже с включёнными экранами.

## Связи

- Официальная интеграция с **`@react-navigation/native`** / drawer / native stack.
- [`entry-03-register-root.md`](./entry-03-register-root.md) следует далее в файле.

## Риски для агентов

Не отключать без причины: возможны регрессии по памяти/жестам навигации. При отладке «пустых» экранов проверить совместимость с кастомными overlay.
