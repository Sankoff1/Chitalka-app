package com.chitalka.screens.common

/** Общая вёрстка списка + FAB для экранов «Читаю сейчас», «Книги и документы», «Избранное». */
object BookListScreenLayout {
    const val LIST_CONTENT_PADDING_DP: Int = 16
    const val EMPTY_TEXT_MARGIN_TOP_DP: Int = 48
    const val EMPTY_TEXT_PADDING_HORIZONTAL_DP: Int = 24
    const val EMPTY_TEXT_FONT_SP: Int = 16
    const val EMPTY_TEXT_LINE_HEIGHT_SP: Int = 22
    const val FAB_SIZE_DP: Int = 56
    const val FAB_RIGHT_INSET_DP: Int = 20
    const val FAB_BOTTOM_INSET_BASE_DP: Int = 16
    const val FAB_LIST_EXTRA_GAP_DP: Int = 16
    const val FAB_ICON_SIZE_DP: Int = 30
    const val FAB_PRESSED_OPACITY: Float = 0.9f
    const val FAB_ELEVATION_DP: Int = 4

    fun fabBottomOffsetDp(safeInsetBottomDp: Int): Int =
        safeInsetBottomDp + FAB_BOTTOM_INSET_BASE_DP

    fun listContentBottomPaddingDp(safeInsetBottomDp: Int): Int =
        fabBottomOffsetDp(safeInsetBottomDp) + FAB_SIZE_DP + FAB_LIST_EXTRA_GAP_DP
}
