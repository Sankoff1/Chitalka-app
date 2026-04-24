package com.chitalka.screens.favorites

import com.chitalka.i18n.AppLocale
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesScreenSpecTest {

    @Test
    fun listContentBottomPadding_noFab() {
        assertEquals(32, FavoritesScreenSpec.listContentBottomPaddingDp(16))
    }

    @Test
    fun emptyListMessageRu() {
        val ru = AppLocale.RU
        assertEquals(
            "Пока нет избранных книг. Откройте меню книги в списке и добавьте её в избранное.",
            FavoritesScreenSpec.emptyListMessage(ru, false),
        )
        assertEquals(
            "Ничего не найдено. Попробуйте изменить запрос.",
            FavoritesScreenSpec.emptyListMessage(ru, true),
        )
    }
}
