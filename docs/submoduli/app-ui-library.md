---
id: app-ui-library
tags: [compose, book-list, trash]
module: app
path: app/.../ui/library/
---

# Модуль `app/` — подмодуль `ui/library/`

## Расположение

`chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ui/library/`

| Файл | Назначение |
|------|------------|
| `ChitalkaLibraryListPane.kt` | Списки книг (сейчас читаю / все / избранное): загрузка через `StorageService`, карточка и действия по спекам Kotlin-модуля, открытие читалки через контроллер. |
| `ChitalkaTrashPane.kt` | Экран корзины по `TrashScreenSpec`, операции с удалёнными книгами. |

## Связи

| Направление | Кто |
|-------------|-----|
| → | [lib-android-storage.md](lib-android-storage.md), [lib-kotlin-screens.md](lib-kotlin-screens.md), [lib-kotlin-ui.md](lib-kotlin-ui.md) (`BookCard`, `BookActions`), [lib-kotlin-i18n.md](lib-kotlin-i18n.md) |
| ← | [app-ui-yadro.md](app-ui-yadro.md) — `ChitalkaDrawerRouter` для `DrawerScreen.ReadingNow` / `BooksAndDocs` / `Favorites` / `Cart` |
