# Внутренняя единица: эффект автозагрузки демо-EPUB

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Условия запуска

`storageReady`, активен [`debug-autoload-epub.md`](./debug-autoload-epub.md), есть spec, однократный `useRef` guard.

## Действия

`suppressWelcomeForPicker` на время; `runDebugAutoLoadEpubIfNeeded` с `storage`, `locale`, `onImported` (без `openReader` — функция только импортирует книгу, если её нет); затем `refreshBookCount`, dismiss welcome; `finally` снимает suppress. Открытие читалки после импорта отдано в [`library-context-restore-last-open.md`](./library-context-restore-last-open.md): если пользователь перед закрытием был в читалке, ключ `lastOpenBook` остался выставленным и следующий запуск откроет ту же книгу; если он был в меню — остаёмся на «Читаю сейчас».

## Связи

- [`debug-autoload-epub.md`](./debug-autoload-epub.md).

## Риски для агентов

В production при выключенном флаге эффект не трогает библиотеку; не удалять guard на повторный запуск.
