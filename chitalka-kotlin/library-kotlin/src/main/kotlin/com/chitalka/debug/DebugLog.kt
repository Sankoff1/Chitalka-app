package com.chitalka.debug

import java.time.Instant

/** Уровни строки консоли (`DebugLogLevel` в TS). */
enum class DebugLogLevel {
    Log,
    Warn,
    Error,
    Debug,
    Info,
    ;

    /** Строка как в RN: `log`, `warn`, … */
    val wireName: String get() = name.lowercase()
}

/** Одна запись буфера. */
data class DebugLogEntry(
    val ts: Long,
    val level: DebugLogLevel,
    val message: String,
)

private const val MAX_ENTRIES = 4000
private const val EXPORT_HEADER_RULE_LENGTH = 40
private const val EXPORT_LINES_INITIAL_EXTRA = 8

private val lock = Any()
private val entries = ArrayDeque<DebugLogEntry>(MAX_ENTRIES + 1)
private val listeners = mutableSetOf<() -> Unit>()

private fun notifyListeners() {
    val snapshot = synchronized(lock) { listeners.toList() }
    for (fn in snapshot) {
        try {
            fn()
        } catch (_: Exception) {
            /* ignore */
        }
    }
}

/** TS: `debugLogAppend` */
fun debugLogAppend(level: DebugLogLevel, message: String) {
    synchronized(lock) {
        entries.addLast(DebugLogEntry(ts = System.currentTimeMillis(), level = level, message = message))
        while (entries.size > MAX_ENTRIES) {
            entries.removeFirst()
        }
    }
    notifyListeners()
}

/** TS: `debugLogSubscribe` — возвращает отписку. */
fun debugLogSubscribe(listener: () -> Unit): () -> Unit {
    synchronized(lock) {
        listeners.add(listener)
    }
    return {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }
}

/** TS: `debugLogGetSnapshot` — копия массива. */
fun debugLogGetSnapshot(): List<DebugLogEntry> = synchronized(lock) { entries.toList() }

/** TS: `debugLogClear` */
fun debugLogClear() {
    synchronized(lock) {
        entries.clear()
    }
    notifyListeners()
}

/** TS: `debugLogFormatExport` */
fun debugLogFormatExport(): String {
    val currentEntries = debugLogGetSnapshot()
    val lines = ArrayList<String>(currentEntries.size + EXPORT_LINES_INITIAL_EXTRA)
    lines += "Chitalka debug log export"
    lines += "Generated: ${Instant.now()}"
    lines += "Entries: ${currentEntries.size}"
    lines += "—".repeat(EXPORT_HEADER_RULE_LENGTH)
    lines += ""
    for (e in currentEntries) {
        lines += "${Instant.ofEpochMilli(e.ts)}\t[${e.level.wireName}]\t${e.message}"
    }
    return lines.joinToString("\n")
}
