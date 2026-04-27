# Отладочная автозагрузка EPUB

В режиме разработки (`__DEV__`) на **Android и iOS** приложение может автоматически импортировать EPUB из бандла Metro и открыть читалку, **без системного проводника**. Файл берётся из `assets/debug/ebook.demo.epub` и копируется через `expo-asset`, дальше тот же путь, что и при обычном импорте (`importEpubToLibrary` → внутренний `file://`).

## Как пользоваться

1. Положите демо-файл в репозиторий: **`assets/debug/ebook.demo.epub`** (имя важно — его подхватывает `src/debug/debugAutoLoadEpub.ts`).
2. Файл в `.gitignore`, в git он не попадает — каждый разработчик копирует свой экземпляр.
3. Запуск: `npx expo start`, подключение по USB / dev client как обычно.

Временно выключить автозагрузку, **не удаляя код**: в `debugAutoLoadEpub.ts` установите `DEBUG_AUTO_LOAD_EPUB_ENABLED = false`.

## Как полностью убрать эту функцию из проекта

Сделайте по шагам (порядок важен для чистого диффа):

1. **`src/context/LibraryContext.tsx`**  
   Удалите импорт из `../debug/debugAutoLoadEpub` и весь блок `useEffect` с `debugAutoLoadStarted`, `isDebugAutoLoadEpubActive`, `getDebugEpubImportSpec`, `runDebugAutoLoadEpubIfNeeded`, а также `useRef` для `debugAutoLoadStarted`, если он больше нигде не используется.

2. **Удалите только файлы автозагрузки EPUB**  
   `src/debug/debugAutoLoadEpub.ts`, `src/debug/epub-asset.d.ts`.  
   Остальное в `src/debug/` (`DebugLog.ts`, `installConsoleCapture.ts` и т.д.) **не удаляйте** — это общие отладочные утилиты приложения.  
   Этот `README.md` можно удалить вместе с автозагрузкой, если он вам больше не нужен.

3. **`metro.config.js`**  
   Уберите строку `config.resolver.assetExts.push('epub');` и комментарий к ней.

4. **`assets/debug/`**  
   Удалите `ebook.demo.epub` и при желании сам каталог (оставьте `.gitkeep`, если каталог нужен пустым в git).

5. **`.gitignore`**  
   Удалите строку `assets/debug/ebook.demo.epub` (и при желании блок комментария про debug EPUB, если он только для этого файла).

6. **`expo-asset`**  
   Если больше нигде не используется, удалите зависимость (`npm uninstall expo-asset`) и при необходимости уберите плагин `"expo-asset"` из массива `plugins` в `app.json` (осторожно: другие модули Expo могут снова подтянуть его транзитивно).

7. **Запись в базе**  
   Книга с `book_id = debug-ebook-demo` может остаться в SQLite на устройстве. Для чистого теста удалите приложение с устройства или очистите данные / воспользуйтесь экраном обслуживания приложения, если там есть сброс библиотеки.

После удаления модуля проводник снова станет единственным способом добавить EPUB с внешнего носителя.
