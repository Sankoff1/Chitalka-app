---
moduli_section: "§6"
source: "../MODULI-I-KOMPONENTY.md#6-сводка-куда-смотреть-для-типовых-задач"
tags: [task-index, navigation, reader, epub, library]
---

# §6. Сводка: куда смотреть для типовых задач

Оглавление: [MODULI §6](../MODULI-I-KOMPONENTY.md#6-сводка-куда-смотреть-для-типовых-задач)

Таблица из MODULI с привязкой к детальным секциям:

| Задача | Где искать (пути) | Детальная дока |
|--------|-------------------|----------------|
| Сменить корневой граф или экран читалки в стеке | `app/.../ChitalkaNavHost.kt`, `ChitalkaNavigationSetup.kt` | [§2.3](sec-02-3-app-navigatsiya.md) |
| Пункты бокового меню и переключение разделов | `app/.../ChitalkaMainShell.kt`, `ChitalkaDrawerRouter.kt`, `library-kotlin/.../DrawerNavigationSpec.kt` | [§2.3](sec-02-3-app-navigatsiya.md), [§3.3](sec-03-3-library-kotlin-navigatsiya.md) |
| Открытие книги / URL маршрута Reader | `app/.../AppNavRoutes.kt`, `library-kotlin/.../NavTypes.kt`, `RootStackDestination.kt` | [§2.3](sec-02-3-app-navigatsiya.md), [§3.3](sec-03-3-library-kotlin-navigatsiya.md), [§4.3](sec-04-3-library-android-picker-chitalka.md) |
| WebView и полифиллы читалки | `app/.../reader/ChitalkaReaderWebView.kt`, `ReactNativeWebPolyfill.kt` | [§2.4](sec-02-4-app-chitalka.md) |
| JS-мост и сообщения читалки | `library-kotlin/.../readerview/ReaderBridge*.kt` | [§3.6](sec-03-6-library-kotlin-most-chitalki.md) |
| Импорт и разбор EPUB | `library-android/.../epub/`, `ImportEpubToLibrary.kt` | [§4.2](sec-04-2-library-android-epub.md) |
| База и файлы библиотеки | `library-android/.../storage/StorageService.kt` | [§4.1](sec-04-1-library-android-hranilishche.md) |
| Логика списков и фильтров без Android | `library-kotlin/.../library/LibrarySessionState.kt`, `screens/common/` | [§3.2](sec-03-2-library-kotlin-sessiya.md), [§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md) |
| Вкладка «Отладочные логи», копирование, откуда берутся строки | `app/.../ChitalkaDebugLogsPane.kt`, `library-kotlin/.../debug/DebugLog.kt`, `InstallConsoleCapture.kt`, `library-android/.../debug/ChitalkaMirrorLog.kt` | [§3.10](sec-03-10-library-kotlin-otladka.md), [§4.5](sec-04-5-library-android-otladka.md), [§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md) (`DebugLogsScreenSpec`) |

При добавлении новых файлов обновляйте соответствующий пункт [MODULI-I-KOMPONENTY.md](../MODULI-I-KOMPONENTY.md) и файл `docs/moduli-detail/sec-*.md`.
