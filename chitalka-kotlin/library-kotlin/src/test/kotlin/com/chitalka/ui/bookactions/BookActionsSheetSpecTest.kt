package com.chitalka.ui.bookactions

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class BookActionsSheetSpecTest {

    @Test
    fun sheetBottomPadding_appliesMinInsetAndExtra() {
        assertEquals(20, BookActionsSheetSpec.sheetBottomPaddingDp(12))
        assertEquals(24, BookActionsSheetSpec.sheetBottomPaddingDp(16))
        assertEquals(20, BookActionsSheetSpec.sheetBottomPaddingDp(0))
    }

    @Test
    fun animationTokens() {
        assertEquals(220, BookActionsSheetSpec.Animation.OPEN_DURATION_MS)
        assertEquals(0.55f, BookActionsSheetSpec.Animation.BACKDROP_MAX_OPACITY, 0f)
    }

    @Test
    fun favoriteIcon_toggle() {
        assertEquals(
            BookActionsSheetSpec.MaterialIcons.FAVORITE_BORDER,
            BookActionsSheetSpec.favoriteActionIconName(false),
        )
        assertEquals(
            BookActionsSheetSpec.MaterialIcons.FAVORITE,
            BookActionsSheetSpec.favoriteActionIconName(true),
        )
    }

    @Test
    fun favoriteLabelsRu() {
        val ru = AppLocale.RU
        assertEquals("В избранное", BookActionsSheetSpec.favoriteActionLabel(ru, false))
        assertEquals("Убрать из избранного", BookActionsSheetSpec.favoriteActionLabel(ru, true))
    }

    @Test
    fun moveToTrashAndCancelRu() {
        val ru = AppLocale.RU
        assertEquals("В корзину", BookActionsSheetSpec.moveToTrashLabel(ru))
        assertEquals("Отмена", BookActionsSheetSpec.cancelLabel(ru))
        assertEquals("Действия с книгой", BookActionsSheetSpec.sheetTitle(ru))
    }

    @Test
    fun coverPlaceholder_isBookEmoji() {
        assertEquals("📖", BookActionsSheetSpec.COVER_PLACEHOLDER_GLYPH)
    }
}
