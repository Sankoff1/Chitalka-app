package com.chitalka.ui.bookcard

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookCardSpecTest {

    @Test
    fun clampProgressFraction_matchesReactNative() {
        assertEquals(0.0, BookCardSpec.clampProgressFraction(Double.NaN), 0.0)
        assertEquals(0.0, BookCardSpec.clampProgressFraction(Double.POSITIVE_INFINITY), 0.0)
        assertEquals(0.0, BookCardSpec.clampProgressFraction(-0.5), 0.0)
        assertEquals(1.0, BookCardSpec.clampProgressFraction(2.0), 0.0)
        assertEquals(0.35, BookCardSpec.clampProgressFraction(0.35), 1e-9)
    }

    @Test
    fun hasProgressValue_onlyForNonNull() {
        assertFalse(BookCardSpec.hasProgressValue(null))
        assertTrue(BookCardSpec.hasProgressValue(Double.NaN))
    }

    @Test
    fun progressPercentRounded() {
        assertEquals(0, BookCardSpec.progressPercentRounded(Double.NaN))
        assertEquals(34, BookCardSpec.progressPercentRounded(0.335))
    }

    @Test
    fun readPercentLabelRu() {
        assertEquals(
            "35% прочитано",
            BookCardSpec.readPercentLabel(AppLocale.RU, 35),
        )
    }

    @Test
    fun coverHeight_matchesAspectRatio() {
        assertEquals(104, BookCardSpec.Layout.coverHeightDp())
    }

    @Test
    fun textBlockEndPaddingWithMenu() {
        assertEquals(42, BookCardSpec.textBlockEndPaddingWithMenuDp())
    }
}
