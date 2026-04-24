# Внутренняя единица: системная панель Android и статус-бар

**Родительский модуль:** `app-shell` — `App.tsx`  
**Файл кода:** `App.tsx` — компонент `AndroidNavigationBar`, узел `StatusBar` внутри `RootNavigator`

## AndroidNavigationBar

- Только `Platform.OS === 'android'`.
- Эффект 1: `NavigationBar.setBackgroundColorAsync`, `setBehaviorAsync('overlay-swipe')` — best-effort, ошибки глотаются.
- Эффект 2: `setButtonStyleAsync` в зависимости от `mode` темы (`light`/`dark` для кнопок).

## StatusBar

- `expo-status-bar`: стиль `light` / `dark` противоположен фону темы для контраста.

## Связи

- Читает [`theme-context.md`](./theme-context.md) через `useTheme` внутри `RootNavigator`.

## Риски для агентов

`AndroidNavigationBar` объявлен внутри `RootNavigator`, чтобы иметь доступ к `useTheme`; не выносить наружу `ThemeProvider`.
