# Внутренняя единица: `SettingsScreen`

**Родительский модуль:** `screen-settings`  
**Файл кода:** `src/screens/SettingsScreen.tsx`

## Роль

- Экран drawer **`Settings`**: тема приложения, язык интерфейса, версия сборки.
- Заголовок «Настройки» только в **шапке навигации** (`drawer.settings`); в теле экрана дублирующего title нет.

## Секции UI

### Карточка настроек

- Общий блок в стиле приложения: фон `colors.menuBackground`, тонкая рамка, скругление; внутри подписи секций (uppercase) и контролы.

### Тема

- Одна строка: подпись **`settings.darkTheme`** + системный **`Switch`** (`value={mode === 'dark'}`), **`setMode('dark' | 'light')`** из [`theme-context.md`](./theme-context.md).

### Язык

- Триггер: **`Pressable`** с текстом текущей локали, иконки **`expand-more` / `expand-less`**, фон и рамка как у поля выбора (`colors.background`, полупрозрачная рамка через `textSecondary`).
- Обёртка с **`ref` + `collapsable={false}`** для стабильного **`measureInWindow`** на Android.
- По нажатию открывается **`Modal`** (`transparent`, `animationType="none"`):
  - **Подложка закрытия:** полноэкранный **`Pressable`** с **`backgroundColor: 'transparent'`** (без затемнения); тап вне списка вызывает закрытие; **`a11y.dismissOverlay`**.
  - **Список языков:** `Animated.View` (**`react-native-reanimated`**) с **`entering={FadeInUp.duration(165)}`** — появление **сверху вниз** (без `springify`).
  - Позиция: **`left` / `width` / `top`** из якоря с **`Math.round`**, высота окна оценивается константой **`LANGUAGE_MENU_ESTIMATE`** (~97 px: две строки по 48 + разделитель); при нехватке места снизу список открывается **над** полем (`openAbove`).
  - Визуально список **стыкуется с полем**: общая рамка (у триггера снимаются нижние или верхние скругления, у списка — соседняя сторона, `borderTopWidth: 0` или `borderBottomWidth: 0` на стыке), лёгкое перекрытие по **`hairlineWidth`** снизу для стыка линий.
  - Фон списка = **`colors.background`**, та же обводка, что у триггера (`borderDropdown`).

### Строки языка

- Список из **`APP_LOCALES`**: для каждого кода — **`Fragment`** + опционально разделитель (полная ширина), затем **`Pressable`**-строка фиксированной высоты **48**, **`alignItems: 'stretch'`**, колонка текста с вертикальным центрированием.
- На Android у **`Text`**: **`includeFontPadding: false`**, **`textAlignVertical: 'center'`** — выравнивание кириллицы/латиницы.
- Выбранная строка: фон **`interactive`**, текст **`topBar`**, иконка **`check`** (у невыбранной та же иконка с **`opacity: 0`**, чтобы ширина колонки не прыгала).
- Ключи строк: **`settings.languageRu`**, **`settings.languageEn`**; см. [`i18n-locale-json.md`](./i18n-locale-json.md).

### Версия

- **`expo-constants`**: `expoConfig?.version` / `nativeApplicationVersion`, иначе `'—'`.

## Связи

- [`theme-context.md`](./theme-context.md), [`i18n-context-provider.md`](./i18n-context-provider.md), **`react-native-reanimated`**, **`expo-constants`**, **`Modal`**, **`Dimensions`**, **`Pressable`**, **`Fragment`**.

## Риски для агентов

- **`key`** у `Animated.View` меню включает координаты якоря — при смене позиции entering-анимация запускается заново; учитывать при правках.
- Правки **`LANGUAGE_MENU_ESTIMATE`** должны соответствовать реальной высоте двух строк + разделителя, иначе при `openAbove` список уедет относительно поля.
- Не возвращать затемнение без явного запроса: подложка должна оставаться **прозрачной**, иначе ломается ощущение «список из поля».
