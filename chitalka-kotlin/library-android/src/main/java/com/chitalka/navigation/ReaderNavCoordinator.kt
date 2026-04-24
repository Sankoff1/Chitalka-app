package com.chitalka.navigation

import com.chitalka.debug.ChitalkaMirrorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LOG_TAG = "Chitalka"
private const val RETRY_MS = 50L
private const val MAX_ATTEMPTS = 50

/**
 * Аналог RN `navigationRef` + `pendingReader` + [flushReaderNavigationIfPending] / [navigateToReader].
 *
 * Передайте `isNavHostReady` (например `navController.currentDestination != null` или флаг из `NavHost`),
 * и `performNavigateToReader` — вызов `navController.navigate(Reader, bundleOf(...))` или эквивалент.
 *
 * [scope] должен использовать [kotlinx.coroutines.Dispatchers.Main] (как `setTimeout` в JS).
 */
class ReaderNavCoordinator(
    private val scope: CoroutineScope,
    private val isNavHostReady: () -> Boolean,
    private val performNavigateToReader: (ReaderRouteParams) -> Unit,
) {
    private val lock = Any()
    private var pendingReader: ReaderRouteParams? = null
    private var retryJob: Job? = null

    /**
     * Вызывать из `NavHost` / `Navigation` `onReady`, чтобы не терять переход до готовности графа.
     */
    fun flushReaderNavigationIfPending() {
        synchronized(lock) {
            val pending = pendingReader ?: return
            if (!isNavHostReady()) {
                return
            }
            pendingReader = null
            performNavigateToReader(pending)
        }
    }

    /**
     * Запланировать переход в читалку с повторными попытками (50 мс × 50), как в RN.
     */
    fun navigateToReader(bookPath: String, bookId: String) {
        synchronized(lock) {
            pendingReader = ReaderRouteParams(bookPath = bookPath, bookId = bookId)
        }
        flushReaderNavigationIfPending()
        synchronized(lock) {
            if (pendingReader == null) {
                return
            }
        }
        retryJob?.cancel()
        retryJob =
            scope.launch {
                repeat(MAX_ATTEMPTS) {
                    delay(RETRY_MS)
                    flushReaderNavigationIfPending()
                    synchronized(lock) {
                        if (pendingReader == null) {
                            return@launch
                        }
                    }
                }
                ChitalkaMirrorLog.w(LOG_TAG, "[Chitalka] navigateToReader: navigation не стал ready за отведённое время")
                synchronized(lock) {
                    pendingReader = null
                }
            }
    }

    /** Сбросить отложенный переход (например при destroy Activity). */
    fun clearPendingReader() {
        retryJob?.cancel()
        retryJob = null
        synchronized(lock) {
            pendingReader = null
        }
    }
}
