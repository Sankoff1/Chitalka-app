# Внутренняя единица: модуль `lastOpenBook`

**Родительский модуль:** `library-last-open-book`
**Файл кода:** `src/library/lastOpenBook.ts`

## Назначение

Персистит идентификатор книги, которая сейчас открыта в читалке, чтобы после
«холодного» перезапуска приложения (в т.ч. когда ОС убила процесс во время
чтения) автоматически открыть ту же книгу. Если пользователь успел вернуться в
меню, ключ очищен и автооткрытие не срабатывает — остаёмся на вкладке «Читаю
сейчас».

## API

- `setLastOpenBookId(bookId)` — `AsyncStorage.setItem('chitalka_last_open_book_id', bookId)`; пустые/пробельные значения игнорируются.
- `getLastOpenBookId()` — возвращает строку или `null`; при ошибке AsyncStorage возвращает `null`, а не бросает.
- `clearLastOpenBookId()` — `AsyncStorage.removeItem(...)`; best-effort.

Все функции ловят ошибки: отсутствие ключа или недоступность AsyncStorage не должны ломать чтение.

## Как используется

- `ReaderScreenWrapper` (см. [`navigation-reader-wrapper.md`](./navigation-reader-wrapper.md)) — `setLastOpenBookId` на mount по `bookId`, `clearLastOpenBookId` в cleanup.
- `LibraryContext` — на `storageReady` пробует прочитать id и через `storage.getLibraryBook` убедиться, что запись ещё жива (не в корзине), затем зовёт `navigateToReader`. См. [`library-context-restore-last-open.md`](./library-context-restore-last-open.md).

## Риски для агентов

- Не хранить здесь путь к файлу: `file_uri` берём из `StorageService.getLibraryBook(bookId)`, чтобы не восстанавливать устаревший URI после переимпорта.
- Не выставлять ключ на экранах библиотеки — только из читалки, иначе сломается правило «в меню → не открывать».
- Если книга попала в корзину (`deletedAt != null`), восстанавливающий эффект чистит ключ.
