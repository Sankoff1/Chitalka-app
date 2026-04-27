# Внутренняя единица: `FirstLaunchModal`

**Родительский модуль:** `ui-first-launch`  
**Файл кода:** `src/components/FirstLaunchModal.tsx`

## Пропсы

`visible`, `hint` (ошибка импорта), `onDismiss`, `onPickEpub`.

## Поведение

RN `Modal` с полупрозрачным оверлеем, кнопки согласно строкам i18n, область для `hint`.

## Связи

- Управляется из [`library-context-welcome-modal.md`](./library-context-welcome-modal.md).

## Риски для агентов

Stacking с системным пикером на Android — см. suppress в контексте.
