---
moduli_section: "§3.9"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#39-epub--коды-ошибок-общие"
tags: [epub, error-codes, shared-with-android]
---

# §3.9. Модуль `library-kotlin` — коды ошибок EPUB

Оглавление: [MODULI §3.9](../MODULI-I-KOMPONENTY.md#39-epub--коды-ошибок-общие)

| Часть | Путь | Связи |
|-------|------|--------|
| Коды ошибок | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/epub/EpubErrorCodes.kt` | Общие константы/ключи для UI и для слоя `library-android` ([§4.2](sec-04-2-library-android-epub.md)) при отображении ошибок импорта/чтения. |

Нет зависимости на Android; используется из `app` и `library-android` по импорту `com.chitalka.epub`.

## Стабильные коды и произвольные сообщения

- Константы в `EpubErrorCodes.kt` (например пустой spine, таймауты копирования / распаковки / подготовки главы) согласованы со строками в **`ReaderScreenSpec`** и переводами в **i18n** (`reader.errors.*`): UI показывает локализованный текст по **точному совпадению** сообщения исключения.
- Класс **`EpubServiceError`** на Android может нести и **произвольную русскоязычную строку** (нет константы в `EpubErrorCodes`): такие сообщения в читалке передаются в UI **как есть** (см. `ReaderOpenErrorKind.Epub` → ветка `else` в `epubOpenErrorMessage`). Примеры из разбора OCF: «Нет META-INF/container.xml…», «В container.xml не найден full-path к OPF.», «OPF не найден по пути: …». Они **не** заменяются общей фразой «Не удалось прочитать container.xml или OPF…» — эта обёртка в `EpubService.open()` применяется только к неожиданным исключениям, не к `EpubServiceError`.

Подробности потока открытия и дискового I/O: [§4.2](sec-04-2-library-android-epub.md).
