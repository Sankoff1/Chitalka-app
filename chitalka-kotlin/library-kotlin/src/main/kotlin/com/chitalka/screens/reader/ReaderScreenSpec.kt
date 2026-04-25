package com.chitalka.screens.reader

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

/**
 * Экран читалки без WebView / EpubService / Storage (`ReaderScreen.tsx`).
 */
object ReaderScreenSpec {

    enum class ReaderLayerId {
        A,
        B,
    }

    data class ReaderLayerState(
        val chapterIndex: Int,
        val html: String,
        val initialScrollY: Double,
        val token: String,
    )

    sealed class ReaderOpenErrorKind {
        data class Epub(
            val message: String,
        ) : ReaderOpenErrorKind()

        data class Other(
            val message: String?,
        ) : ReaderOpenErrorKind()

        data object Unknown : ReaderOpenErrorKind()
    }

    object I18nKeys {
        const val BACK_TO_LIBRARY = "reader.backToLibrary"
        const val LOADING = "reader.loading"
        const val ERROR_TITLE = "reader.errorTitle"
        const val BACK_TO_BOOKS = "reader.backToBooks"
        const val ERR_EMPTY_SPINE = "reader.errors.emptySpine"
        const val ERR_TIMEOUT_COPY = "reader.errors.timeoutCopy"
        const val ERR_TIMEOUT_UNZIP = "reader.errors.timeoutUnzip"
        const val ERR_TIMEOUT_PREPARE_CHAPTER = "reader.errors.timeoutPrepareChapter"
        const val ERR_OPEN_FAILED = "reader.errors.openFailed"
        const val ERR_UNKNOWN = "reader.errors.unknown"
        const val CHAPTER_PROGRESS = "reader.chapterProgress"
    }

    object Timing {
        /** Автосохранение прогресса после скролла (`setTimeout(..., 500)` в RN). */
        const val SCROLL_PERSIST_DEBOUNCE_MS: Long = 500L

        /**
         * Таймаут ожидания `ready` неактивного слоя перед стартом анимации
         * (`setTimeout(..., 400)` в `waitForPendingLayer`).
         */
        const val PENDING_LAYER_READY_TIMEOUT_MS: Long = 400L

        /** Длительность `Animated.timing` перелистывания глав. */
        const val CHAPTER_TRANSITION_DURATION_MS: Long = 380L

        /**
         * Throttle скролла на стороне приложения после сообщений WebView — см.
         * [com.chitalka.ui.readerview.READER_BRIDGE_SCROLL_DEBOUNCE_MS].
         */
        const val BRIDGE_SCROLL_DEBOUNCE_MS: Long =
            com.chitalka.ui.readerview.READER_BRIDGE_SCROLL_DEBOUNCE_MS
    }

    object Transition {
        /** Доля ширины экрана для сдвига активной / входящей страницы. */
        const val EDGE_TRANSLATE_FRACTION: Float = 0.12f

        private const val PROGRESS_END: Float = 1f
        private const val PROGRESS_START: Float = 0f

        /** Середина интерполяции `activeOpacity` по оси прогресса (RN: 0.6). */
        private const val ACTIVE_OPACITY_MID_PROGRESS: Float = 0.6f

        /** Значение `activeOpacity` в середине (RN: 0.25). */
        private const val ACTIVE_OPACITY_MID_VALUE: Float = 0.25f

        /** До этой доли прогресса входящая страница остаётся с opacity 0 (RN: 0.3). */
        const val INCOMING_INVISIBLE_UNTIL_PROGRESS: Float = 0.3f

        /** `1f` − [INCOMING_INVISIBLE_UNTIL_PROGRESS] — длина спада входящей шейды. */
        const val INCOMING_SHADE_FADE_WIDTH: Float = PROGRESS_END - INCOMING_INVISIBLE_UNTIL_PROGRESS

        /** Допуск совпадения соседних узлов в [piecewiseLinear]. */
        const val PIECEWISE_SEGMENT_EPS: Float = 1e-6f

        /** Ключевые точки `activeOpacity` (input 0..1). */
        val ACTIVE_OPACITY_INPUTS: FloatArray =
            floatArrayOf(PROGRESS_START, ACTIVE_OPACITY_MID_PROGRESS, PROGRESS_END)

        /** Значения `activeOpacity` при входах [ACTIVE_OPACITY_INPUTS]. */
        val ACTIVE_OPACITY_OUTPUTS: FloatArray =
            floatArrayOf(PROGRESS_END, ACTIVE_OPACITY_MID_VALUE, PROGRESS_START)

        /** Ключевые точки `incomingOpacity`. */
        val INCOMING_OPACITY_INPUTS: FloatArray =
            floatArrayOf(PROGRESS_START, INCOMING_INVISIBLE_UNTIL_PROGRESS, PROGRESS_END)

        val INCOMING_OPACITY_OUTPUTS: FloatArray =
            floatArrayOf(PROGRESS_START, PROGRESS_START, PROGRESS_END)

        /** Исходящая «шейда» поверх уходящей страницы: 0 → 0.08. */
        const val OUTGOING_SHADE_END: Float = 0.08f

        /** Входящая шейда: пик 0.06 при progress ≈ [INCOMING_INVISIBLE_UNTIL_PROGRESS]. */
        const val INCOMING_SHADE_PEAK: Float = 0.06f
    }

    object Layout {
        const val ERROR_SCREEN_PADDING_TOP_EXTRA_DP: Int = 24
        const val ERROR_SCREEN_PADDING_DP: Int = 24
        const val ERROR_TITLE_FONT_SP: Int = 18
        const val ERROR_TITLE_MARGIN_BOTTOM_DP: Int = 12
        const val ERROR_BODY_FONT_SP: Int = 15
        const val ERROR_BODY_LINE_HEIGHT_SP: Int = 22
        const val ERROR_BACK_MARGIN_TOP_DP: Int = 20
        const val ERROR_BACK_PADDING_VERTICAL_DP: Int = 12
        const val ERROR_BACK_PADDING_HORIZONTAL_DP: Int = 18
        const val ERROR_BACK_CORNER_RADIUS_DP: Int = 8
        const val ERROR_BACK_PRESSED_OPACITY: Float = 0.88f
        const val ERROR_BACK_BACKGROUND_LIGHT_HEX: String = "#e8e6e1"

        const val LIBRARY_BAR_PADDING_HORIZONTAL_DP: Int = 8
        const val LIBRARY_BAR_PADDING_TOP_DP: Int = 6
        const val LIBRARY_BAR_PADDING_BOTTOM_DP: Int = 4
        const val LIBRARY_LINK_PADDING_VERTICAL_DP: Int = 8
        const val LIBRARY_LINK_PADDING_HORIZONTAL_DP: Int = 8
        const val LIBRARY_LINK_CORNER_RADIUS_DP: Int = 8
        const val LIBRARY_LINK_PRESSED_OPACITY: Float = 0.85f
        const val LIBRARY_LINK_DISABLED_OPACITY: Float = 0.45f
        const val LIBRARY_LINK_TEXT_FONT_SP: Int = 16

        const val PAGE_INDICATOR_PADDING_TOP_DP: Int = 6
        const val PAGE_INDICATOR_PADDING_BOTTOM_MIN_DP: Int = 8
        const val PAGE_INDICATOR_TEXT_FONT_SP: Int = 13

        const val LOADER_GAP_DP: Int = 12
        const val LOADER_TEXT_FONT_SP: Int = 15

        const val PAGE_LAYER_SHADOW_RADIUS_DP: Int = 12
        const val PAGE_LAYER_ELEVATION_DP: Int = 12
    }

    object Colors {
        const val HAIRLINE_DARK_MODE: String = "rgba(255,255,255,0.12)"
        const val HAIRLINE_LIGHT_MODE: String = "rgba(0,0,0,0.12)"
        const val READER_FRAME_BACKGROUND_LIGHT_HEX: String = "#ece9e1"
        const val READER_PAPER_BACKGROUND_LIGHT_HEX: String = "#ffffff"
        const val PAGE_SHADE_HEX: String = "#000000"
    }

    const val EMPTY_READER_HTML: String =
        "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body></body></html>"

    fun backToLibrary(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.BACK_TO_LIBRARY)

    fun loading(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.LOADING)

    fun errorTitle(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.ERROR_TITLE)

    fun backToBooks(locale: AppLocale): String = I18nCatalog.tSync(locale, I18nKeys.BACK_TO_BOOKS)

    fun chapterProgressLabel(
        locale: AppLocale,
        currentOneBased: Int,
        totalChapters: Int,
    ): String =
        I18nCatalog.tSync(
            locale,
            I18nKeys.CHAPTER_PROGRESS,
            mapOf("current" to currentOneBased, "total" to totalChapters),
        )

    /**
     * Текст индикатора как в RN: «`current+1`/`spine.length`» (без i18n-шаблона).
     */
    fun pageIndicatorSlash(
        zeroBasedChapterIndex: Int,
        spineLength: Int,
    ): String = "${zeroBasedChapterIndex + 1}/$spineLength"
}
