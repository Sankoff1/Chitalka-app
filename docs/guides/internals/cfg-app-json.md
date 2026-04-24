# Внутренняя единица: `app.json`

**Родительский модуль:** `cfg-app-json`  
**Файл:** `app.json`

## Назначение

Конфигурация Expo: имя приложения, slug, иконки, splash, ориентация, список плагинов (`expo-build-properties` и др.).

## Связи

- Версия для UI настроек может дублироваться с полями Expo — см. [`screen-settings.md`](./screen-settings.md).

## Риски для агентов

Смена `package` / bundle id влияет на магазины и deep links; править осознанно.
