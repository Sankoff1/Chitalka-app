# Внутренняя единица: баррель `src/theme/index.ts`

**Родительский модуль:** `theme-barrel`  
**Файл кода:** `src/theme/index.ts`

## Экспорты

- Из **`colors.ts`**: `lightThemeColors`, `darkThemeColors`, `getColorsForMode`, типы `ThemeColors`, `ThemeMode`.
- Из **`ThemeContext.tsx`**: `ThemeProvider`, `useTheme`.

Персистенция режима темы живёт **только** в [`theme-context.md`](./theme-context.md) (AsyncStorage); баррель её не трогает.

## Связи

- Потребители: почти все экраны и компоненты с визуальной темой.

## Риски для агентов

Минимальный файл — новую логику (storage, побочные эффекты) добавлять в `ThemeContext.tsx` или отдельные модули, а сюда только реэкспорт публичного API.
