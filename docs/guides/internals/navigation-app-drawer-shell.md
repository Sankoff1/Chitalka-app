# Внутренняя единица: каркас `AppDrawer`

**Родительский модуль:** `nav-drawer`  
**Файл кода:** `src/navigation/AppDrawer.tsx`

## Ширина drawer

`min(288, windowWidth - 24)` через `useWindowDimensions`.

## `screenOptions`

- Кастомный `header` — [`navigation-app-top-bar.md`](./navigation-app-top-bar.md).
- `drawerStyle`, цвета активных/неактивных пунктов из [`theme-colors.md`](./theme-colors.md) / `useTheme`.

## Экраны

Список `Drawer.Screen` с компонентами и `options` из [`i18n-catalog.md`](./i18n-catalog.md) / `useI18n` (мемоизированные строки).

## Связи

- [`navigation-app-drawer-placeholders.md`](./navigation-app-drawer-placeholders.md).

## Риски для агентов

Порядок экранов влияет на начальный маршрут по умолчанию drawer.
