# Внутренняя единица: палитры `colors.ts`

**Родительский модуль:** `theme-colors`  
**Файл кода:** `src/theme/colors.ts`

## Содержимое

- `lightThemeColors`, `darkThemeColors` — набор семантических цветов UI.
- `getColorsForMode(mode)` — выбор палитры.
- Типы `ThemeColors`, `ThemeMode`.

## Связи

- [`theme-context.md`](./theme-context.md).

## Риски для агентов

Новый цвет добавлять в **обе** палитры для консистентности.

Персистится не файл `colors.ts`, а только выбор **`ThemeMode`** в [`theme-context.md`](./theme-context.md); палитры остаются статичными в коде.
