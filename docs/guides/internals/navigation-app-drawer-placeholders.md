# Внутренняя единица: состав экранов `AppDrawer`

**Родительский модуль:** `nav-drawer`  
**Файл кода:** `src/navigation/AppDrawer.tsx`

## Экраны

Все пункты drawer подключены **отдельными файлами** из `src/screens/` (без inline-заглушек):

| Маршрут (`DrawerParamList`) | Компонент | Подписи drawer |
|-----------------------------|-----------|----------------|
| `ReadingNow` | `ReadingNowScreen` | `drawer.readingNow` |
| `BooksAndDocs` | `BooksAndDocsScreen` | `drawer.books` |
| `Favorites` | `FavoritesScreen` | `drawer.favorites` |
| `Cart` | `TrashScreen` | `drawer.cart` |
| `DebugLogs` | `DebugLogsScreen` | `drawer.debugLogs` |
| `Settings` | `SettingsScreen` | `drawer.settings` |

## Каркас

Ширина drawer, `screenOptions`, кастомный header — в [`navigation-app-drawer-shell.md`](./navigation-app-drawer-shell.md).

## Риски для агентов

Имя маршрута `Cart` исторически; в UI это корзина удалённых книг (`TrashScreen`), не корзина покупок.
