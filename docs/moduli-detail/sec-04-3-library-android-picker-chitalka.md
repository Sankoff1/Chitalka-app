---
moduli_section: "§4.3"
module: library-android
source: "../MODULI-I-KOMPONENTY.md#43-системный-выбор-файла-и-навигация-к-читалке"
tags: [ActivityResultContracts, EpubPickerAndroid, ReaderNavCoordinator]
---

# §4.3. Модуль `library-android` — picker и навигация к читалке

Оглавление: [MODULI §4.3](../MODULI-I-KOMPONENTY.md#43-системный-выбор-файла-и-навигация-к-читалке)

| Часть | Путь | Связи |
|-------|------|--------|
| Выбор EPUB | `chitalka-kotlin/library-android/src/main/java/com/chitalka/picker/EpubPickerAndroid.kt` | MIME/контракт, `mapOpenDocumentUri` → `EpubPickResult` ([§3.8](sec-03-8-library-kotlin-picker.md)); вызывается из `ChitalkaApp` ([§2.2](sec-02-2-app-compose-koren.md)). |
| Координатор читалки | `chitalka-kotlin/library-android/src/main/java/com/chitalka/navigation/ReaderNavCoordinator.kt` | Отложенный `navigateToReader` до готовности `NavHost`; параметры [§3.3](sec-03-3-library-kotlin-navigatsiya.md); wiring в [§2.3](sec-02-3-app-navigatsiya.md). |
