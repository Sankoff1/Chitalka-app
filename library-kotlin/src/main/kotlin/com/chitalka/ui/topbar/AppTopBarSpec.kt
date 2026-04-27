package com.chitalka.ui.topbar

import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nCatalog
import com.chitalka.navigation.DrawerScreen

/** Поведение верхней панели: показ поиска, кнопок drawer, авто-закрытие поиска при смене экрана. */
object AppTopBarSpec {

    /** Маршруты, где поиск по библиотеке скрыт. */
    val nonSearchableRouteNames: Set<String> =
        setOf(
            DrawerScreen.Settings.routeName,
            DrawerScreen.DebugLogs.routeName,
        )

    fun isDrawerRouteSearchable(routeName: String): Boolean =
        routeName !in nonSearchableRouteNames

    /** Состояние библиотеки, влияющее на видимость кнопок поиска. */
    data class SearchChromeState(
        val bookCount: Int,
        val isSearchOpen: Boolean,
        val searchQuery: String,
    )

    fun shouldShowSearchButton(
        routeName: String,
        state: SearchChromeState,
    ): Boolean =
        isDrawerRouteSearchable(routeName) && state.bookCount > 0 && !state.isSearchOpen

    fun shouldShowSearchInput(
        routeName: String,
        state: SearchChromeState,
    ): Boolean =
        isDrawerRouteSearchable(routeName) && state.isSearchOpen

    fun shouldShowClearQueryButton(
        routeName: String,
        state: SearchChromeState,
    ): Boolean =
        shouldShowSearchInput(routeName, state) && state.searchQuery.isNotEmpty()

    /** При уходе на экран без поиска нужно автоматически закрыть поле. */
    fun shouldAutoCloseSearchForRoute(
        routeName: String,
        isSearchOpen: Boolean,
    ): Boolean =
        !isDrawerRouteSearchable(routeName) && isSearchOpen

    object I18nKeys {
        const val A11Y_OPEN_MENU = "a11y.openMenu"
        const val A11Y_SEARCH = "a11y.search"
        const val A11Y_CLOSE_SEARCH = "a11y.closeSearch"
        const val SEARCH_PLACEHOLDER = "search.placeholder"
    }

    object Layout {
        const val ROW_MIN_HEIGHT_DP: Int = 53
        const val ROW_PADDING_HORIZONTAL_DP: Int = 4
        const val SIDE_SLOT_WIDTH_DP: Int = 48
        const val TITLE_WRAP_PADDING_HORIZONTAL_DP: Int = 8
        const val TITLE_FONT_SIZE_SP: Int = 18
        const val INPUT_FONT_SIZE_SP: Int = 17
        const val ICON_HIT_SLOP_DP: Int = 12
        const val ICON_BUTTON_PADDING_DP: Int = 8
        const val MENU_ICON_SIZE_DP: Int = 26
        const val CLOSE_ICON_SIZE_DP: Int = 24
        const val PRESSED_OPACITY: Float = 0.82f
        const val BOTTOM_BORDER_ALPHA: Float = 0.12f
    }

    /** Задержка фокуса поля поиска после открытия. */
    const val SEARCH_INPUT_FOCUS_DELAY_MS: Long = 50L

    data class Strings(
        val a11yOpenMenu: String,
        val a11ySearch: String,
        val a11yCloseSearch: String,
        val searchPlaceholder: String,
    )

    fun strings(locale: AppLocale): Strings =
        Strings(
            a11yOpenMenu = I18nCatalog.tSync(locale, I18nKeys.A11Y_OPEN_MENU),
            a11ySearch = I18nCatalog.tSync(locale, I18nKeys.A11Y_SEARCH),
            a11yCloseSearch = I18nCatalog.tSync(locale, I18nKeys.A11Y_CLOSE_SEARCH),
            searchPlaceholder = I18nCatalog.tSync(locale, I18nKeys.SEARCH_PLACEHOLDER),
        )
}
