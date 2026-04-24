# Внутренняя единица: `ThemeProvider` и `useTheme`

**Родительский модуль:** `theme-context`  
**Файл кода:** `src/theme/ThemeContext.tsx`

## Состояние

`mode: ThemeMode` в `useState(initialMode)` — по умолчанию `'light'` из пропа провайдера.

## Вычисление

`colors = useMemo(() => getColorsForMode(mode), [mode])`.

## API

`setMode`, `toggleTheme` (светлая ↔ тёмная).

## Персистенция

Отсутствует в этом файле — тема сбрасывается при перезапуске приложения.

## Риски для агентов

Не путать с системной темой ОС — пользовательский выбор только light/dark из настроек.
