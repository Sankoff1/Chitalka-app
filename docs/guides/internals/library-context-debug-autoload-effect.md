# Внутренняя единица: эффект автозагрузки демо-EPUB

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Условия запуска

`storageReady`, активен [`debug-autoload-epub.md`](./debug-autoload-epub.md), есть spec, однократный `useRef` guard.

## Действия

`suppressWelcomeForPicker` на время; `runDebugAutoLoadEpubIfNeeded` с `storage`, `locale`, `openReader`, `onImported`; затем `refreshBookCount`, dismiss welcome; `finally` снимает suppress.

## Связи

- [`debug-autoload-epub.md`](./debug-autoload-epub.md).

## Риски для агентов

В production при выключенном флаге эффект не трогает библиотеку; не удалять guard на повторный запуск.
