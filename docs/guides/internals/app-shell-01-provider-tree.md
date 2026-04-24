# Внутренняя единица: внешнее дерево провайдеров

**Родительский модуль:** `app-shell` — `App.tsx`  
**Файл кода:** `App.tsx`, функция `App` (default export)

## Назначение

Оборачивает всё приложение: `SafeAreaProvider` → `ThemeProvider` → `I18nProvider` → `RootNavigator`.

## Инварианты

- Тема и i18n доступны **внутри** `RootNavigator` и всех экранов.
- `SafeAreaProvider` снаружи — инсеты для `SafeAreaView` / хуков.

## Связи

- [`theme-context.md`](./theme-context.md), [`i18n-context-provider.md`](./i18n-context-provider.md), [`app-shell-03-navigation-composition.md`](./app-shell-03-navigation-composition.md).

## Риски для агентов

Новые глобальные провайдеры обычно добавляют сюда или сразу под `NavigationContainer` осознанно (порядок контекста важен).
