---
moduli_section: "§4.5"
module: library-android
source: "../MODULI-I-KOMPONENTY.md#45-отладка-на-устройстве"
tags: [debug, auto-epub, runDebugAutoLoadEpubIfNeeded, ChitalkaMirrorLog]
---

# §4.5. Модуль `library-android` — отладка на устройстве

Оглавление: [MODULI §4.5](../MODULI-I-KOMPONENTY.md#45-отладка-на-устройстве)

| Часть | Путь | Связи |
|-------|------|--------|
| Runner автозагрузки | `chitalka-kotlin/library-android/src/main/java/com/chitalka/debug/DebugAutoLoadEpubRunner.kt` | `suspend fun runDebugAutoLoadEpubIfNeeded` — вызывается из `ChitalkaApp` ([§2.2](sec-02-2-app-compose-koren.md)) с `engineeringBuild = BuildConfig.DEBUG`; использует правила из [§3.10](sec-03-10-library-kotlin-otladka.md) `DebugAutoLoadEpub.kt`, импорт [§4.2](sec-04-2-library-android-epub.md), хранилище [§4.1](sec-04-1-library-android-hranilishche.md). Файл из assets: [§2.5](sec-02-5-app-resursy-testy.md) (`debug/ebook.demo.epub`). |
| Зеркало `Log` в буфер отладки | `chitalka-kotlin/library-android/src/main/java/com/chitalka/debug/ChitalkaMirrorLog.kt` | Внутренний API: вызовы `android.util.Log` в `library-android` идут через обёртку и дублируются в `debugLogAppend` ([§3.10](sec-03-10-library-kotlin-otladka.md) `DebugLog.kt`), чтобы строки были видны во вкладке «Отладочные логи» в `app` ([§3.4](sec-03-4-library-kotlin-spetsifikatsii-ekranov.md) `DebugLogsScreenSpec`, [§2.4](sec-02-4-app-chitalka.md) UI панели). |
