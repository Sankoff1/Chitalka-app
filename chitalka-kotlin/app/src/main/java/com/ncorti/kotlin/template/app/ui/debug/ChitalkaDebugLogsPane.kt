@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.chitalka.debug.DebugLogEntry
import com.chitalka.debug.debugLogClear
import com.chitalka.debug.debugLogFormatExport
import com.chitalka.debug.debugLogGetSnapshot
import com.chitalka.debug.debugLogSubscribe
import com.chitalka.i18n.AppLocale
import com.chitalka.screens.debuglogs.DebugLogsScreenSpec
import com.ncorti.kotlin.template.app.BuildConfig
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEBUG_LOG_RELOAD_COALESCE_MS = 120L

@Composable
fun ChitalkaDebugLogsPane(locale: AppLocale) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entries = remember { mutableStateListOf<DebugLogEntry>() }
    var exporting by remember { mutableStateOf(false) }

    fun reload() {
        val snap = debugLogGetSnapshot()
        entries.clear()
        entries.addAll(snap)
    }

    DisposableEffect(Unit) {
        reload()
        val pending = AtomicBoolean(false)
        val unsub = debugLogSubscribe {
            if (pending.compareAndSet(false, true)) {
                scope.launch {
                    delay(DEBUG_LOG_RELOAD_COALESCE_MS)
                    pending.set(false)
                    reload()
                }
            }
        }
        onDispose { unsub() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            DebugLogsScreenSpec.title(locale),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            DebugLogsScreenSpec.subtitle(locale),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DebugActionButton(
                icon = Icons.Filled.Clear,
                label = DebugLogsScreenSpec.clearLabel(locale),
                enabled = !exporting && entries.isNotEmpty(),
                onClick = {
                    debugLogClear()
                    reload()
                },
            )
            DebugActionButton(
                icon = Icons.Filled.Add,
                label = DebugLogsScreenSpec.copyLabel(locale),
                enabled = !exporting && entries.isNotEmpty(),
                onClick = {
                    val body = debugLogFormatExport()
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(DebugLogsScreenSpec.title(locale), body))
                },
            )
            DebugActionButton(
                icon = Icons.Filled.Share,
                label = if (exporting) "…" else DebugLogsScreenSpec.exportLabel(locale),
                enabled = !exporting && entries.isNotEmpty(),
                onClick = {
                    exporting = true
                    scope.launch {
                        try {
                            exportLogsToShare(context, locale)
                        } catch (_: Exception) {
                        } finally {
                            exporting = false
                        }
                    }
                },
            )
        }
        Spacer(Modifier.size(12.dp))
        if (entries.isEmpty()) {
            Text(
                DebugLogsScreenSpec.emptyLabel(locale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    itemsIndexed(
                        entries,
                        key = { i, e -> DebugLogsScreenSpec.listItemKey(e.ts, i) },
                    ) { _, item ->
                        LogLine(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(label)
    }
}

@Composable
private fun LogLine(item: DebugLogEntry) {
    val (badgeColor, onBadge) = levelColors(item.level.wireName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            item.level.wireName,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            color = onBadge,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            item.message,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun levelColors(level: String): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (level.uppercase()) {
        "E", "ERROR" -> scheme.errorContainer to scheme.onErrorContainer
        "W", "WARN" -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        "I", "INFO" -> scheme.primaryContainer to scheme.onPrimaryContainer
        else -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
}

private suspend fun exportLogsToShare(context: Context, locale: AppLocale) {
    val body = debugLogFormatExport()
    val name = DebugLogsScreenSpec.exportFileName()
    val file = File(context.cacheDir, name)
    withContext(Dispatchers.IO) {
        file.writeText(body, Charsets.UTF_8)
    }
    val uri =
        FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file,
        )
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = DebugLogsScreenSpec.EXPORT_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, DebugLogsScreenSpec.exportDialogTitle(locale))
        }
    val chooserTitle = DebugLogsScreenSpec.exportDialogTitle(locale)
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
