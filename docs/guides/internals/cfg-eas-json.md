# Внутренняя единица: `eas.json`

**Родительский модуль:** `cfg-eas`  
**Файл:** `eas.json`

## Профили `build`

- `development` — dev client, internal distribution.
- `preview` — internal, Android APK.
- `production` — `autoIncrement` версии (источник версии remote в `cli`).

## `submit`

Профиль `production` для отправки в сторы.

## Риски для агентов

Сборка на EAS должна соответствовать `app.json` и секретам на сервере Expo — не дублировать чувствительные данные в репозиторий.
