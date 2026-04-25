package com.chitalka.theme

import com.chitalka.library.LastOpenBookPersistence

/** Ключ хранилища для режима темы. */
const val THEME_MODE_STORAGE_KEY = "chitalka_theme_mode"

/** Снимок для UI: режим и палитра. Палитры статичны (см. [getColorsForMode]) — кэшировать не нужно. */
data class ThemeUiState(
    val mode: ThemeMode,
) {
    val colors: ThemeColors
        get() = getColorsForMode(mode)
}

/** Чтение сохранённого режима. Невалидное / отсутствующее → `null` (вызывающий применит дефолт). */
suspend fun loadPersistedThemeMode(storage: LastOpenBookPersistence): ThemeMode? =
    try {
        val stored = storage.getItem(THEME_MODE_STORAGE_KEY) ?: return null
        ThemeMode.fromCode(stored)
    } catch (_: Exception) {
        null
    }

/** Сохранение режима; ошибки глотаются — потеря настройки не критична. */
suspend fun persistThemeMode(storage: LastOpenBookPersistence, mode: ThemeMode) {
    try {
        storage.setItem(THEME_MODE_STORAGE_KEY, mode.code)
    } catch (_: Exception) {
        // best-effort
    }
}

/** Переключение light ↔ dark. */
fun ThemeMode.toggle(): ThemeMode =
    when (this) {
        ThemeMode.LIGHT -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.LIGHT
    }

/** Переключить режим и сохранить. Возвращает новый режим. */
suspend fun togglePersistedThemeMode(storage: LastOpenBookPersistence, current: ThemeMode): ThemeMode {
    val next = current.toggle()
    persistThemeMode(storage, next)
    return next
}
