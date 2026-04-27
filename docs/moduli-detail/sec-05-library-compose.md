---
moduli_section: "§5"
module: library-compose
source: "../MODULI-I-KOMPONENTY.md#5-модуль-library-compose-не-используется-приложением"
tags: [compose-template, factorial, unused-by-app]
---

# §5. Модуль `library-compose` (не в сборке `app`)

Оглавление: [MODULI §5](../MODULI-I-KOMPONENTY.md#5-модуль-library-compose-не-используется-приложением)

| Часть | Путь | Связи |
|-------|------|--------|
| Activity | `chitalka-kotlin/library-compose/src/main/java/com/ncorti/kotlin/template/app/ComposeActivity.kt` | Демо UI; не объявена в манифесте `app`. |
| Composable | `chitalka-kotlin/library-compose/src/main/java/com/ncorti/kotlin/template/app/ui/components/Factorial.kt` | Импорт [§3.11](sec-03-11-library-kotlin-shablon.md) `FactorialCalculator`. |
| Манифест | `chitalka-kotlin/library-compose/src/main/AndroidManifest.xml` | Для потребителей AAR как отдельной библиотеки. |
| Тест | `chitalka-kotlin/library-compose/src/androidTest/java/com/ncorti/kotlin/template/app/FactorialTest.kt` | UI-тест демо. |

Gradle: `library-compose` → `library-kotlin` ([§1](sec-01-gradle-sostav.md)). Обзор: [modules/library-compose.md](../modules/library-compose.md).
