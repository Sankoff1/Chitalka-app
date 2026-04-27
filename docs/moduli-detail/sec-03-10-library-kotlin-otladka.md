---
moduli_section: "§3.10"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#310-отладка-и-вспомогательные-утилиты"
tags: [debug, DebugLog, WithTimeout, console-capture]
---

# §3.10. Модуль `library-kotlin` — отладка и утилиты

Оглавление: [MODULI §3.10](../MODULI-I-KOMPONENTY.md#310-отладка-и-вспомогательные-утилиты)

| Часть | Путь | Связи |
|-------|------|--------|
| Лог | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/DebugLog.kt` | UI панели логов в `app` (`ChitalkaDebugLogsPane`, [§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md) DebugLogsScreenSpec). |
| Перехват консоли | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/InstallConsoleCapture.kt` | `ChitalkaApplication` ([§2.1](sec-02-1-app-zapusk.md)): **stdout/stderr** → буфер. Вызовы **`android.util.Log`** на устройстве дополнительно зеркалируются в буфер из `library-android` — [§4.5](sec-04-5-library-android-otladka.md) (`ChitalkaMirrorLog`). |
| Автозагрузка EPUB (правила) | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/debug/DebugAutoLoadEpub.kt` | Данные/флаги; исполнение на устройстве — [§4.5](sec-04-5-library-android-otladka.md). |
| Таймаут | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/utils/WithTimeout.kt` | `withTimeout` используется в `EpubService` ([§4.2](sec-04-2-library-android-epub.md)). |

Тесты: `DebugLogTest`, `InstallConsoleCaptureTest`, `DebugAutoLoadEpubTest`, `WithTimeoutTest`.
