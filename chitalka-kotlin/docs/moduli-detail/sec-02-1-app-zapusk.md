---
moduli_section: "§2.1"
module: app
source: "../MODULI-I-KOMPONENTY.md#21-запуск-и-жизненный-цикл-процесса"
tags: [android, Application, Activity, Manifest, lifecycle]
---

# §2.1. Модуль `app` — запуск и жизненный цикл процесса

Оглавление: [MODULI §2.1](../MODULI-I-KOMPONENTY.md#21-запуск-и-жизненный-цикл-процесса)

## Файлы и связи

| Часть | Путь | Назначение и связи |
|-------|------|---------------------|
| Класс `Application` | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/ChitalkaApplication.kt` | Точка процесса; `installConsoleCapture` ([§3.10](sec-03-10-library-kotlin-otladka.md)) перенаправляет **stdout/stderr** в буфер отладки. Сообщения **`android.util.Log`** в этот буфер попадают через зеркало в [§4.5](sec-04-5-library-android-otladka.md). |
| Главная Activity | `chitalka-kotlin/app/src/main/java/com/ncorti/kotlin/template/app/MainActivity.kt` | `setContent { ChitalkaApp(this) }` → весь UI в [§2.2](sec-02-2-app-compose-koren.md). |
| Манифест | `chitalka-kotlin/app/src/main/AndroidManifest.xml` | Регистрация `Application`, `MainActivity`, разрешения/тема. |
| Разметка XML | `chitalka-kotlin/app/src/main/res/layout/activity_main.xml` | Классическая View-разметка; основной UI идёт через Compose из `MainActivity`. |

## Зависимости модулей

`app` → `library-android`, `library-kotlin` (см. [§1](sec-01-gradle-sostav.md)).
