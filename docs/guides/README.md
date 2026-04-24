# Документация для агентов (Chitalka-app)

Цель: чтобы ИИ-агенты и разработчики быстро восстанавливали контекст **архитектуры, контрактов и подводных камней** без обхода всего репозитория.

## С чего начать

1. **[Карта модулей и связей](../MODULES.md)** — сухой индекс файлов, ID модулей и Mermaid-схема зависимостей.
2. **[Микро-модули (внутренние единицы)](./internals/README.md)** — отдельный `.md` на каждую внутреннюю часть (функции, эффекты, классы, конфиги).
3. Ниже — **углубленные** заметки по областям; читайте в любом порядке, но логичная цепочка: вход → данные → UI → инфраструктура.

## Файлы по областям

| Файл | Содержание |
|------|------------|
| [01-entry-app-navigation.md](./01-entry-app-navigation.md) | `index.ts`, `App.tsx`, провайдеры, React Navigation (`types`, `navigationRef`, stack, drawer, `ReaderScreenWrapper`), `AppTopBar`. |
| [02-domain-data-epub.md](./02-domain-data-epub.md) | `core/types`, `StorageService`, `EpubService`, `importEpubToLibrary`, `withTimeout`, `epubPipelineAndroid`, `epubPicker`. |
| [03-library-context-ui-screens.md](./03-library-context-ui-screens.md) | `LibraryContext`, компоненты (`ReaderView`, карточки, модалки), все экраны и потоки чтения/импорта в UI. |
| [04-i18n-theme-debug-config.md](./04-i18n-theme-debug-config.md) | Локализация, тема, отладочный лог и автозагрузка EPUB, `app.json` / EAS / Metro / TS / скрипты. |

## Правило поддержки

При **существенном** изменении поведения модуля обновите соответствующий файл в `docs/guides/`, при дроблении логики — файл в `docs/guides/internals/`, и при необходимости строку в `docs/MODULES.md`.
