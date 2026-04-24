# Внутренняя единица: `tsconfig.json`

**Родительский модуль:** `cfg-ts`  
**Файл:** `tsconfig.json`

## Назначение

Настройки компилятора TypeScript для Expo/React Native (extends, strictness, paths если заданы).

## Риски для агентов

Смена `moduleResolution` или `jsx` может сломать сборку Metro; следовать шаблону Expo.
