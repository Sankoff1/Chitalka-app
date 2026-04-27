---
moduli_section: "§3.8"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#38-выбор-epub-чистая-логика-без-activity"
tags: [EpubPickResult, EpubPickerUtils, picker, pure-kotlin]
---

# §3.8. Модуль `library-kotlin` — выбор EPUB (логика без Activity)

Оглавление: [MODULI §3.8](../MODULI-I-KOMPONENTY.md#38-выбор-epub-чистая-логика-без-activity)

| Часть | Путь | Связи |
|-------|------|--------|
| Результат выбора | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/picker/EpubPickResult.kt` | Sealed: Ok / Error / Canceled; заполняется из [§4.3](sec-04-3-library-android-picker-chitalka.md) `EpubPickerAndroid.mapOpenDocumentUri`. |
| Утилиты URI | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/picker/EpubPickerUtils.kt` | Разбор путей без `ContentResolver` (тестируемая логика). |

Поток: `ChitalkaApp` ([§2.2](sec-02-2-app-compose-koren.md)) → launcher Android → `EpubPickResult` → `importEpubToLibrary` ([§4.2](sec-04-2-library-android-epub.md)).

Тест: `EpubPickerUtilsTest.kt`.
