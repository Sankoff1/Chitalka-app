# Внутренняя единица: каркас `AppDrawer`

**Родительский модуль:** `nav-drawer`  
**Файл кода:** `src/navigation/AppDrawer.tsx`

## Ширина drawer

`min(288, windowWidth - 24)` через `useWindowDimensions`.

## `screenOptions`

- Кастомный `header` — [`navigation-app-top-bar.md`](./navigation-app-top-bar.md).
- `drawerStyle`, цвета активных/неактивных пунктов из [`theme-colors.md`](./theme-colors.md) / `useTheme`.

## Экраны

Список `Drawer.Screen` с компонентами из `src/screens/` и `options` из `useI18n` (мемоизированные строки `drawer.*`). Перечень маршрутов — в [`navigation-app-drawer-placeholders.md`](./navigation-app-drawer-placeholders.md).

## Связи

- [`navigation-app-drawer-placeholders.md`](./navigation-app-drawer-placeholders.md) — таблица маршрут ↔ экран.

## Риски для агентов

Порядок экранов влияет на начальный маршрут по умолчанию drawer.
