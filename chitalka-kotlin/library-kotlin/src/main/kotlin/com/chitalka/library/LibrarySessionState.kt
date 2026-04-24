package com.chitalka.library

/**
 * Изменяемое состояние уровня приложения из `LibraryContext.tsx` без React/Alert/picker.
 * Доступ с одного потока (обычно main); синхронизацию для фоновых вызовов обеспечивает вызывающий.
 */
@Suppress("TooManyFunctions")
class LibrarySessionState(
    initialLibraryEpoch: Int = 0,
    initialBookCount: Int = 0,
    initialStorageReady: Boolean = false,
) {
    var bookCount: Int = initialBookCount
        private set

    var storageReady: Boolean = initialStorageReady
        private set

    /** Увеличивается после импорта и др. — подписка UI на обновление списков. */
    var libraryEpoch: Int = initialLibraryEpoch
        private set

    var isSearchOpen: Boolean = false
        private set

    var searchQuery: String = ""
        private set

    /** Сессионный флаг: пользователь закрыл приветствие пустой библиотеки. */
    var welcomeDismissedThisSession: Boolean = false
        private set

    /** Подсказка ошибки на модалке первого запуска (после picker и т.п.). */
    var welcomePickerHint: String? = null
        private set

    /**
     * На Android системный picker не должен перекрываться модалкой —
     * временно скрываем welcome (аналог `suppressWelcomeForPicker` в RN).
     */
    var suppressWelcomeForPicker: Boolean = false
        private set

    fun bumpLibraryEpoch() {
        libraryEpoch += 1
    }

    fun markStorageReady(ready: Boolean = true) {
        storageReady = ready
    }

    fun updateBookCount(count: Long) {
        bookCount = count.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }

    fun openSearch() {
        isSearchOpen = true
    }

    fun closeSearch() {
        isSearchOpen = false
        searchQuery = ""
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
    }

    fun dismissWelcomeModal() {
        welcomeDismissedThisSession = true
        welcomePickerHint = null
    }

    fun setWelcomePickerHint(hint: String?) {
        welcomePickerHint = hint
    }

    fun setSuppressWelcomeForPicker(suppress: Boolean) {
        suppressWelcomeForPicker = suppress
    }

    /** Видимость `FirstLaunchModal` — та же формула, что `welcomeModalVisible` в RN. */
    fun isFirstLaunchWelcomeVisible(): Boolean =
        storageReady && bookCount == 0 && !welcomeDismissedThisSession && !suppressWelcomeForPicker
}
