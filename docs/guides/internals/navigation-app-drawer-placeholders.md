# Внутренняя единица: встроенные экраны-заглушки в drawer

**Родительский модуль:** `nav-drawer`  
**Файл кода:** `src/navigation/AppDrawer.tsx`

## Компоненты

Локальные функции-экраны: `ReadingNowScreen`, `FavoritesScreen`, `AuthorsScreen`, `CollectionsScreen`, `CartScreen` — каждый рендерит [`screen-placeholder.md`](./screen-placeholder.md) с заголовком/подзаголовком из `t('screens.*')`.

## Исключение

`BooksAndDocs`, `DebugLogs`, `Settings` — полноценные экраны из `src/screens/`.

## Риски для агентов

Не путать с отдельными файлами в `screens/` — логика объявлена inline в `AppDrawer.tsx`.
