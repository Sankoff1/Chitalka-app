---
moduli_section: "§3.6"
module: library-kotlin
source: "../MODULI-I-KOMPONENTY.md#36-мост-нативного-слоя-к-web-читалке-скрипты-сообщения-тема-страницы"
tags: [readerview, WebView-bridge, ReaderBridge, dark-mode-html]
---

# §3.6. Модуль `library-kotlin` — мост к Web-читалке

Оглавление: [MODULI §3.6](../MODULI-I-KOMPONENTY.md#36-мост-нативного-слоя-к-web-читалке-скрипты-сообщения-тема-страницы)

| Часть | Путь | Связи |
|-------|------|--------|
| JS-мост | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderBridgeScripts.kt` | Инжектится из `ChitalkaReaderWebView` ([§2.4](sec-02-4-app-chitalka.md)). |
| Сообщения | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderBridgeMessages.kt` | `ReaderBridgeInboundMessage`, парсинг JSON из WebView. |
| Тёмная тема HTML | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderDarkModeHtml.kt` | `injectDarkReaderHead` + палитра [§3.7](sec-03-7-library-kotlin-i18n-tema.md). |
| Направление страниц | `chitalka-kotlin/library-kotlin/src/main/kotlin/com/chitalka/ui/readerview/ReaderPageDirection.kt` | LTR/RTL для постраничной навигации. |

Тесты: `ReaderBridge*Test.kt`, `ReaderDarkModeHtmlTest.kt` в `src/test/kotlin/com/chitalka/ui/readerview/`.
