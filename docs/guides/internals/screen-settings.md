# Внутренняя единица: `SettingsScreen`

**Родительский модуль:** `screen-settings`  
**Файл кода:** `src/screens/SettingsScreen.tsx`

## Секции

- Тема: две кнопки `light` / `dark` → `setMode` из [`theme-context.md`](./theme-context.md).
- Язык: выбор из `APP_LOCALES`, `setLocale`.
- Версия приложения: `expo-constants` (`expoConfig.version` / `nativeApplicationVersion`).

## Связи

- [`theme-context.md`](./theme-context.md), [`i18n-context-provider.md`](./i18n-context-provider.md).

## Риски для агентов

Режим темы **не** персистится в `ThemeContext` (только `useState`) — при перезапуске снова `initialMode` из провайдера.
