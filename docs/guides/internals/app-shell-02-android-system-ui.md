# Внутренняя единица: системная панель Android и статус-бар

**Родительский модуль:** `app-shell` — `App.tsx`  
**Файл кода:** `App.tsx` — компонент `AndroidNavigationBar`, узел `StatusBar` внутри `RootNavigator`

## AndroidNavigationBar

- Только `Platform.OS === 'android'`.
- Эффект 1: `NavigationBar.setBackgroundColorAsync`, `setBehaviorAsync('overlay-swipe')` — best-effort, ошибки глотаются.
- Эффект 2 (`mode` темы): перед **`setButtonStyleAsync`** планируется вызов через **`requestAnimationFrame`**, в cleanup — **`cancelAnimationFrame`** и флаг `cancelled`, чтобы не обновлять стиль кнопок после размонтирования и реже ловить гонку с первым кадром после смены темы.

## StatusBar

- `expo-status-bar`: стиль `light` / `dark` противоположен фону темы для контраста.

## Связи

- Читает [`theme-context.md`](./theme-context.md) через `useTheme` внутри `RootNavigator`.

## Риски для агентов

`AndroidNavigationBar` объявлен внутри `RootNavigator`, чтобы иметь доступ к `useTheme`; не выносить наружу `ThemeProvider`.
