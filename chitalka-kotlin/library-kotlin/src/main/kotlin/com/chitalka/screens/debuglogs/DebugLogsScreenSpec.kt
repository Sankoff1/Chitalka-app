@file:Suppress("TooManyFunctions")

package com.chitalka.screens.debuglogs

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import java.time.Instant

/**
 * Экран отладочных логов без FlatList / FileSystem / Sharing (`DebugLogsScreen.tsx`).
 */
object DebugLogsScreenSpec {

    object I18nKeys {
        const val TITLE = "debugLogs.title"
        const val SUBTITLE = "debugLogs.subtitle"
        const val CLEAR = "debugLogs.clear"
        const val COPY = "debugLogs.copy"
        const val EXPORT = "debugLogs.export"
        const val EMPTY = "debugLogs.empty"
        const val EXPORT_FAILED = "debugLogs.exportFailed"
        const val EXPORT_NO_CACHE = "debugLogs.exportNoCache"
        const val EXPORT_SAVED = "debugLogs.exportSaved"
        const val EXPORT_DIALOG_TITLE = "debugLogs.exportDialogTitle"
    }

    const val EXPORT_MIME_TYPE: String = "text/plain"
    const val EXPORT_ENCODING_UTF8: String = "UTF-8"

    object Layout {
        const val ROOT_PADDING_HORIZONTAL_DP: Int = 16
        const val TITLE_FONT_SP: Int = 22
        const val TITLE_MARGIN_BOTTOM_DP: Int = 6
        const val SUBTITLE_FONT_SP: Int = 14
        const val SUBTITLE_LINE_HEIGHT_SP: Int = 20
        const val SUBTITLE_MARGIN_BOTTOM_DP: Int = 14
        const val TOOLBAR_GAP_DP: Int = 10
        const val TOOLBAR_MARGIN_BOTTOM_DP: Int = 12
        const val BTN_PADDING_VERTICAL_DP: Int = 12
        const val BTN_CORNER_RADIUS_DP: Int = 10
        const val BTN_MIN_HEIGHT_DP: Int = 48
        const val BTN_PRESSED_OPACITY: Float = 0.88f
        const val BTN_DISABLED_OPACITY: Float = 0.45f
        const val BTN_TEXT_FONT_SP: Int = 16
        const val LIST_CONTENT_PADDING_BOTTOM_DP: Int = 24
        const val LINE_FONT_SP: Int = 12
        const val LINE_MARGIN_BOTTOM_DP: Int = 6
        const val LINE_LINE_HEIGHT_SP: Int = 16
        const val EMPTY_MARGIN_TOP_DP: Int = 32
        const val EMPTY_FONT_SP: Int = 15
    }

    object FlatListTuning {
        const val INITIAL_NUM_TO_RENDER: Int = 24
        const val MAX_TO_RENDER_PER_BATCH: Int = 32
        const val WINDOW_SIZE: Int = 10
    }

    /** Между уровнем и текстом в строке списка. */
    const val LINE_LEVEL_MESSAGE_SEPARATOR: String = "  "

    fun title(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.TITLE)

    fun subtitle(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.SUBTITLE)

    fun clearLabel(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.CLEAR)

    fun copyLabel(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.COPY)

    fun exportLabel(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.EXPORT)

    fun emptyLabel(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.EMPTY)

    fun exportFailedPrefix(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.EXPORT_FAILED)

    fun exportNoCacheMessage(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.EXPORT_NO_CACHE)

    fun exportSavedPrefix(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.EXPORT_SAVED)

    fun exportDialogTitle(locale: AppLocale): String =
        I18nCatalog.tSync(locale, I18nKeys.EXPORT_DIALOG_TITLE)

    /** Как `keyExtractor`: `${item.ts}-${index}`. */
    fun listItemKey(
        ts: Long,
        index: Int,
    ): String = "$ts-$index"

    /** Кнопки панели (очистить / скопировать / экспорт) неактивны при экспорте или пустом буфере. */
    fun toolbarActionsDisabled(
        exporting: Boolean,
        entryCount: Int,
    ): Boolean = exporting || entryCount == 0

    /**
     * Имя файла экспорта: `chitalka-logs-${ISO.replace(/[:.]/g, '-')}.txt`.
     */
    fun exportFileName(now: Instant = Instant.now()): String {
        val iso = now.toString()
        val safe = iso.replace(":", "-").replace(".", "-")
        return "chitalka-logs-$safe.txt"
    }

    /** Сборка `cacheDir` + имя с завершающим слэшем как в RN. */
    fun exportFilePathInCache(
        cacheDir: String,
        fileName: String,
    ): String {
        val base =
            if (cacheDir.endsWith("/")) {
                cacheDir
            } else {
                "$cacheDir/"
            }
        return "$base$fileName"
    }
}
