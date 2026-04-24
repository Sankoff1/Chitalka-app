package com.chitalka.navigation

import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.library.clearLastOpenBookId
import com.chitalka.library.setLastOpenBookId

/**
 * Поведение обёртки читалки над корневым стеком (`ReaderScreenWrapper.tsx`):
 * ключ последней книги, счётчик библиотеки, `goBack`.
 *
 * **Важно:** [onReaderStackBeforeRemove] вызывать только из навигационного `beforeRemove` при уходе со стека
 * читалки (back / `popBackStack`). Не вызывать `clearLastOpenBookId` из `onDispose` / `DisposableEffect` —
 * при пересборке Compose ключ должен сохраняться (аналог комментария в RN про JS-reload).
 */
object ReaderRouteLifecycle {
    /** При входе на маршрут читалки — запомнить id для автооткрытия на следующем запуске. */
    suspend fun onReaderEntered(
        persistence: LastOpenBookPersistence,
        bookId: String,
    ) {
        setLastOpenBookId(persistence, bookId)
    }

    /** Перед снятием экрана читалки со стека навигации — очистить ключ «последняя книга». */
    suspend fun onReaderStackBeforeRemove(persistence: LastOpenBookPersistence) {
        clearLastOpenBookId(persistence)
    }

    /** Кнопка «в библиотеку»: обновить счётчик книг и выполнить возврат по стеку. */
    suspend fun onBackToLibrary(
        refreshBookCount: suspend () -> Unit,
        goBack: () -> Unit,
    ) {
        refreshBookCount()
        goBack()
    }

    /** После открытия книги в читалке (`onOpened` в RN). */
    suspend fun onReaderContentOpened(refreshBookCount: suspend () -> Unit) {
        refreshBookCount()
    }
}
