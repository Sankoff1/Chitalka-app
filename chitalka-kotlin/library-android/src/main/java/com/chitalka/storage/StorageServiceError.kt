package com.chitalka.storage

/**
 * Ошибка слоя хранилища (аналог [StorageServiceError] в RN).
 */
class StorageServiceError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
