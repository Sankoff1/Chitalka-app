# Внутренняя единица: `ReaderScreenWrapper`

**Родительский модуль:** `nav-reader-wrapper`  
**Файл кода:** `src/navigation/ReaderScreenWrapper.tsx`

## Назначение

Связывает типизированные `route.params` (`bookPath`, `bookId`) с [`screen-reader-render-phases.md`](./screen-reader-render-phases.md).

## Колбэки

- `onBackToLibrary`: `refreshBookCount()` из [`library-context-contract.md`](./library-context-contract.md) + `navigation.goBack()`.
- `onOpened`: только `refreshBookCount` после успешного открытия (см. ReaderScreen).

## Эффект `lastOpenBook`

Два отдельных `useEffect`:

- по `bookId` — `setLastOpenBookId(bookId)` на монтировании (без cleanup-очистки);
- подписка на событие навигации `beforeRemove` — `clearLastOpenBookId()` только когда пользователь уводит экран (back-жест, `navigation.goBack()` из `onBackToLibrary`).

Файл: [`library-last-open-book.md`](./library-last-open-book.md). Важно: на обычный React-unmount завязываться **нельзя** — JS-reload и kill процесса штатно демонтируют экран, и `return () => clearLastOpenBookId()` стёр бы ключ, сломав автооткрытие. Событие `beforeRemove` — это navigation action, оно срабатывает только при явном уходе пользователя, а при reload/kill не возникает вовсе, поэтому ключ корректно переживает перезапуск.

## Риски для агентов

Не передавать сюда нестабильный URI из пикера — только после импорта. Не перекладывать `setLastOpenBookId`/`clearLastOpenBookId` на cleanup `useEffect` или на `onBackToLibrary` (hardware back тогда не очистит ключ). Если добавляете новые способы покинуть читалку, проверьте, что они проходят через React Navigation и триггерят `beforeRemove`.
