# Внутренняя единица: `ThemeProvider` и `useTheme`

**Родительский модуль:** `theme-context`  
**Файл кода:** `src/theme/ThemeContext.tsx`

## Зависимости

- React (`createContext`, хуки).
- **`@react-native-async-storage/async-storage`** — чтение/запись выбранного режима.
- Типы и **`getColorsForMode`** из [`theme-colors.md`](./theme-colors.md).

## Состояние

- Внутренний стейт: **`useState<ThemeMode>(initialMode)`** (`setModeState` в коде).
- Проп **`initialMode`** (по умолчанию `'light'`) задаёт первый рендер до асинхронного чтения storage.
- После монтирования **`useEffect`** читает AsyncStorage; если значение строго **`'light'`** или **`'dark'`**, вызывается **`setModeState`** — сохранённый режим заменяет стартовый (паттерн как у локали в [`i18n-context-provider.md`](./i18n-context-provider.md)).
- Невалидные или отсутствующие значения в storage **игнорируются**; при ошибке `getItem`/`setItem` состояние UI не ломается (ошибки глотаются).

## Вычисление

`colors = getColorsForMode(mode)` — объекты палитр в [`theme-colors.md`](./theme-colors.md) статичны; отдельный `useMemo` на каждый рендер не используется (смена ссылки только при смене `mode`).

## API

- **`setMode(mode)`** — установить режим и **записать** его в storage.
- **`toggleTheme()`** — светлая ↔ тёмная с **записью** в storage.

## Персистенция

| | |
|--|--|
| **Ключ** | `chitalka_theme_mode` (рядом по смыслу с `chitalka_locale` в i18n). |
| **Значения** | Строки `'light'` \| `'dark'`. |
| **Чтение** | Один раз при монтировании провайдера. |
| **Запись** | При каждом **`setMode`** и **`toggleTheme`**. |

**Первый кадр:** до завершения `getItem` на экране может кратко отображаться **`initialMode`**, затем без дополнительного запроса применится сохранённый режим.

## Риски для агентов

- Не путать с **системной** темой ОС — в приложении только ручной выбор light/dark (например, [`screen-settings.md`](./screen-settings.md)).
- Не смешивать с i18n: язык и тема — **разные** ключи и провайдеры.
- Менять ключ storage только осознанно (миграция / сброс настроек у пользователей).
