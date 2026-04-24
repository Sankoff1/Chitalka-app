package com.ncorti.kotlin.template.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.i18n.APP_LOCALES
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.theme.ThemeMode
import com.chitalka.i18n.persistLocale
import com.chitalka.theme.persistThemeMode
import com.ncorti.kotlin.template.app.BuildConfig
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@Composable
fun ChitalkaSettingsPane(
    persistence: LastOpenBookPersistence,
    i18n: I18nUiState,
    locale: AppLocale,
    themeMode: ThemeMode,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(Modifier.padding(16.dp)) {
        Text(i18n.t("settings.themeSection"), style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(i18n.t("settings.darkTheme"), modifier = Modifier.weight(1f))
            Switch(
                checked = themeMode == ThemeMode.DARK,
                onCheckedChange = { dark ->
                    val next = if (dark) ThemeMode.DARK else ThemeMode.LIGHT
                    scope.launch {
                        persistThemeMode(persistence, next)
                        onThemeModeChange(next)
                    }
                },
            )
        }
        Text(
            i18n.t("settings.languageSection"),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp),
        )
        APP_LOCALES.forEach { loc ->
            Button(
                onClick = {
                    scope.launch {
                        persistLocale(persistence, loc)
                        onLocaleChange(loc)
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    loc.name,
                    fontWeight = if (loc == locale) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
        Text(
            "${i18n.t("settings.versionLabel")} ${BuildConfig.VERSION_NAME}",
            modifier = Modifier.padding(top = 32.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
