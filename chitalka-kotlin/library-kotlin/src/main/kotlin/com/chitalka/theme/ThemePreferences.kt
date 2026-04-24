package com.chitalka.theme

import com.chitalka.library.LastOpenBookPersistence

/**
 * Ключ AsyncStorage в RN (`ThemeContext.tsx`); тот же контракт для Android KV.
 */
const val THEME_MODE_STORAGE_KEY = "chitalka_theme_mode"

/**
 * Снимок для UI: режим и палитра без лишнего кэширования — палитры статичны в [getColorsForMode].
 */
data class ThemeUiState(
    val mode: ThemeMode,
) {
    val colors: ThemeColors
        get() = getColorsForMode(mode)
}

/**
 * Чтение сохранённого режима. Невалидные или отсутствующие значения → `null` (как в RN при ошибке/неизвестном stored).
 */
suspend fun loadPersistedThemeMode(storage: LastOpenBookPersistence): ThemeMode? =
    try {
        val stored = storage.getItem(THEME_MODE_STORAGE_KEY) ?: return null
        ThemeMode.fromCode(stored)
    } catch (_: Exception) {
        null
    }

/** Сохранение режима; ошибки глотаются (best-effort, как `persistMode` в RN). */
suspend fun persistThemeMode(storage: LastOpenBookPersistence, mode: ThemeMode) {
    try {
        storage.setItem(THEME_MODE_STORAGE_KEY, mode.code)
    } catch (_: Exception) {
        /* best-effort */
    }
}

/** Переключение light ↔ dark (аналог `toggleTheme` без React state). */
fun ThemeMode.toggle(): ThemeMode =
    when (this) {
        ThemeMode.LIGHT -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.LIGHT
    }

/**
 * Переключить режим и сохранить; возвращает новый режим (часть логики `toggleTheme` в RN).
 */
suspend fun togglePersistedThemeMode(storage: LastOpenBookPersistence, current: ThemeMode): ThemeMode {
    val next = current.toggle()
    persistThemeMode(storage, next)
    return next
}
