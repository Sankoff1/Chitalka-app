# Внутренняя единица: `RootStack`

**Родительский модуль:** `nav-root-stack`  
**Файл кода:** `src/navigation/RootStack.tsx`

## Состав

Native stack с `headerShown: false`:

- `Main` → [`navigation-app-drawer-shell.md`](./navigation-app-drawer-shell.md).
- `Reader` → [`navigation-reader-wrapper.md`](./navigation-reader-wrapper.md).

## Связи

- Типы из [`navigation-types-param-lists.md`](./navigation-types-param-lists.md).

## Риски для агентов

Новый корневой экран добавлять сюда и в `RootStackParamList`.
