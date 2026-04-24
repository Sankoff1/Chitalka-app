package com.chitalka.theme

/** Режим темы (`'light' | 'dark'` в RN). */
enum class ThemeMode(val code: String) {
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromCode(code: String): ThemeMode? =
            entries.find { it.code.equals(code, ignoreCase = false) }
    }
}

/**
 * Палитра в hex-строках (#RRGGBB), как в React Native.
 */
data class ThemeColors(
    /** Основной задник экрана */
    val background: String,
    /** Кликабельные элементы (кнопки, акценты) */
    val interactive: String,
    /** Верхняя панель (header) */
    val topBar: String,
    /** Фон бокового меню */
    val menuBackground: String,
    /** Текст на топ-баре */
    val topBarText: String,
    /** Основной текст контента */
    val text: String,
    /** Второстепенный текст */
    val textSecondary: String,
)

val lightThemeColors = ThemeColors(
    background = "#EBFADD",
    interactive = "#9FDE75",
    topBar = "#2A7833",
    menuBackground = "#F8FCEE",
    topBarText = "#FFFFFF",
    text = "#1A2E1C",
    textSecondary = "#4A5F4C",
)

val darkThemeColors = ThemeColors(
    background = "#172016",
    interactive = "#39513A",
    topBar = "#00480A",
    menuBackground = "#222C20",
    topBarText = "#FFFFFF",
    text = "#E6F0E2",
    textSecondary = "#A8B8A5",
)

fun getColorsForMode(mode: ThemeMode): ThemeColors =
    when (mode) {
        ThemeMode.DARK -> darkThemeColors
        ThemeMode.LIGHT -> lightThemeColors
    }
