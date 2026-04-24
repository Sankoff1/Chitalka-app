package com.chitalka.ui.readerview

/** Направление смены главы из страницы (`ReaderPageDirection` в `ReaderView.tsx`). */
enum class ReaderPageDirection(
    val wire: String,
) {
    PREV("prev"),
    NEXT("next"),
    ;

    companion object {
        fun fromWire(value: String): ReaderPageDirection? =
            entries.find { it.wire == value }
    }
}
