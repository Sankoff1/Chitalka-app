package com.chitalka.screens.reader

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog

/**
 * Контракт экрана читалки: типы слоёв, тайминги анимации, i18n-ключи и геометрия.
 * Не зависит от WebView / EpubService / Storage — чистый Kotlin.
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
        /** Автосохранение прогресса после скролла. */
        const val SCROLL_PERSIST_DEBOUNCE_MS: Long = 500L

        /** Таймаут ожидания `ready` неактивного слоя перед стартом анимации перелистывания. */
        const val PENDING_LAYER_READY_TIMEOUT_MS: Long = 400L

        /** Длительность анимации перелистывания глав. */
        const val CHAPTER_TRANSITION_DURATION_MS: Long = 380L

        /** Throttle обработки scroll-сообщений из WebView. */
        const val BRIDGE_SCROLL_DEBOUNCE_MS: Long =
            com.chitalka.ui.readerview.READER_BRIDGE_SCROLL_DEBOUNCE_MS
    }

    object Transition {
        /** Доля ширины экрана для сдвига активной / входящей страницы. */
        const val EDGE_TRANSLATE_FRACTION: Float = 0.12f

        private const val PROGRESS_END: Float = 1f
        private const val PROGRESS_START: Float = 0f

        /** Середина интерполяции `activeOpacity` по оси прогресса. */
        private const val ACTIVE_OPACITY_MID_PROGRESS: Float = 0.6f

        /** Значение `activeOpacity` в середине. */
        private const val ACTIVE_OPACITY_MID_VALUE: Float = 0.25f

        /** До этой доли прогресса входящая страница остаётся с opacity 0. */
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
        const val PAGE_INDICATOR_PADDING_TOP_DP: Int = 6
        const val PAGE_INDICATOR_PADDING_BOTTOM_MIN_DP: Int = 8
        const val PAGE_INDICATOR_TEXT_FONT_SP: Int = 13
    }

    object Colors {
        const val READER_FRAME_BACKGROUND_LIGHT_HEX: String = "#ece9e1"
        const val READER_PAPER_BACKGROUND_LIGHT_HEX: String = "#ffffff"
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

    /** Текст индикатора страницы вида «`current+1`/`spine.length`» — без i18n-шаблона. */
    fun pageIndicatorSlash(
        zeroBasedChapterIndex: Int,
        spineLength: Int,
    ): String = "${zeroBasedChapterIndex + 1}/$spineLength"

    fun normalizeSavedScrollRangeMax(raw: Double?): Double {
        val v = raw ?: return 0.0
        return if (v.isFinite() && v >= 0.0) v else 0.0
    }
}
