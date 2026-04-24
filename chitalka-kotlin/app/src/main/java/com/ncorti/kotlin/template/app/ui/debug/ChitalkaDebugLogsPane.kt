@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChitalkaDebugLogsPane(locale: AppLocale) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entries = remember { mutableStateListOf<DebugLogEntry>() }
    var exporting by remember { mutableStateOf(false) }

    fun reload() {
        entries.clear()
        entries.addAll(debugLogGetSnapshot())
    }

    DisposableEffect(Unit) {
        reload()
        val unsub = debugLogSubscribe { reload() }
        onDispose { unsub() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(DebugLogsScreenSpec.title(locale), style = MaterialTheme.typography.titleLarge)
        Text(
            DebugLogsScreenSpec.subtitle(locale),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Button(
                onClick = {
                    debugLogClear()
                    reload()
                },
                enabled = !exporting && entries.isNotEmpty(),
            ) {
                Text(DebugLogsScreenSpec.clearLabel(locale))
            }
            Button(
                onClick = {
                    val body = debugLogFormatExport()
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(DebugLogsScreenSpec.title(locale), body))
                },
                enabled = !exporting && entries.isNotEmpty(),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(DebugLogsScreenSpec.copyLabel(locale))
            }
            Button(
                onClick = {
                    exporting = true
                    scope.launch {
                        try {
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
                        } catch (_: Exception) {
                        } finally {
                            exporting = false
                        }
                    }
                },
                enabled = !exporting && entries.isNotEmpty(),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(if (exporting) "…" else DebugLogsScreenSpec.exportLabel(locale))
            }
        }
        if (entries.isEmpty()) {
            Text(
                DebugLogsScreenSpec.emptyLabel(locale),
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                itemsIndexed(entries, key = { i, e -> DebugLogsScreenSpec.listItemKey(e.ts, i) }) { _, item ->
                    Text(
                        "${item.level.wireName}${DebugLogsScreenSpec.LINE_LEVEL_MESSAGE_SEPARATOR}${item.message}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
