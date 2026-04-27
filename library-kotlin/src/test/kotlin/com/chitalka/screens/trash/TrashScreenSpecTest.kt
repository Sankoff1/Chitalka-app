package com.chitalka.screens.trash

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashScreenSpecTest {

    @Test
    fun emptyListMessageRu() {
        val ru = AppLocale.RU
        assertEquals("Корзина пуста.", TrashScreenSpec.emptyListMessage(ru, false))
        assertEquals(
            "Ничего не найдено. Попробуйте изменить запрос.",
            TrashScreenSpec.emptyListMessage(ru, true),
        )
    }

    @Test
    fun listContentBottomPadding() {
        assertEquals(32, TrashScreenSpec.listContentBottomPaddingDp(16))
    }

    @Test
    fun formatFileSizeMbLineRu() {
        val line = TrashScreenSpec.formatFileSizeMbLine(2_097_152L, AppLocale.RU)
        assertTrue(line.startsWith("2.00"))
        assertTrue(line.endsWith("МБ"))
    }

}
