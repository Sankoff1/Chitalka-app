# Внутренняя единица: `ReaderScreenWrapper`

**Родительский модуль:** `nav-reader-wrapper`  
**Файл кода:** `src/navigation/ReaderScreenWrapper.tsx`

## Назначение

Связывает типизированные `route.params` (`bookPath`, `bookId`) с [`screen-reader-render-phases.md`](./screen-reader-render-phases.md).

## Колбэки

- `onBackToLibrary`: `refreshBookCount()` из [`library-context-contract.md`](./library-context-contract.md) + `navigation.goBack()`.
- `onOpened`: только `refreshBookCount` после успешного открытия (см. ReaderScreen).

## Риски для агентов

Не передавать сюда нестабильный URI из пикера — только после импорта.
