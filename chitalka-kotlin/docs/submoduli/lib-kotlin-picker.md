---
id: lib-kotlin-picker
tags: [EpubPickResult, uri-utils]
module: library-kotlin
package: com.chitalka.picker
---

# `library-kotlin` — подмодуль `com.chitalka.picker`

## Расположение

`chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/picker/`

| Файл | Роль |
|------|------|
| `EpubPickResult.kt` | Результат выбора файла: успех с URI/id, ошибка с ключом строки, отмена. |
| `EpubPickerUtils.kt` | Чистые функции разбора URI/путей **без** Android API — удобно тестировать. |

## Связи

| Направление | Кто |
|-------------|-----|
| Заполняет `EpubPickResult` | [lib-android-picker.md](lib-android-picker.md) после `ActivityResult` |
| Потребляет результат | [app-ui-yadro.md](app-ui-yadro.md) в `ChitalkaApp` → далее [lib-android-library.md](lib-android-library.md) |

Тесты: [lib-kotlin-testy.md](lib-kotlin-testy.md) (`EpubPickerUtilsTest`).
