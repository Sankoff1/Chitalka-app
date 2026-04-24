# Внутренняя единица: приветственная модалка и `suppressWelcomeForPicker`

**Родительский модуль:** `library-context`  
**Файл кода:** `src/context/LibraryContext.tsx`

## Условие видимости `welcomeModalVisible`

`storageReady && bookCount === 0 && !welcomeDismissedSession && !suppressWelcomeForPicker`.

## `suppressWelcomeForPicker`

На Android системный document picker часто **не показывается поверх RN Modal** — перед пикером флаг `true` скрывает [`ui-first-launch-modal.md`](./ui-first-launch-modal.md); после — `false`.

## `FirstLaunchModal`

Рендерится sibling к `children` провайдера; пропсы `visible`, `hint`, `onDismiss`, `onPickEpub`.

## Связи

- [`library-context-pick-welcome.md`](./library-context-pick-welcome.md).

## Риски для агентов

Не убирать задержку/подавление без проверки на реальном Android.
