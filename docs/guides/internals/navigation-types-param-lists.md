# Внутренняя единица: типы маршрутов

**Родительский модуль:** `nav-types`  
**Файл кода:** `src/navigation/types.ts`

## `DrawerParamList`

Имена экранов drawer без параметров (`undefined`).

## `RootStackParamList`

- `Main`: вложенные параметры drawer (`NavigatorScreenParams<DrawerParamList>`).
- `Reader`: `{ bookPath: string; bookId: string }` — стабильный путь к EPUB и id в БД.

## Связи

- Используются в [`navigation-root-stack.md`](./navigation-root-stack.md), [`navigation-ref-container-and-flush.md`](./navigation-ref-container-and-flush.md), типах навигации в экранах.

## Риски для агентов

Любое новое поле в `Reader` потребует обновления всех `navigate('Reader', …)`.
