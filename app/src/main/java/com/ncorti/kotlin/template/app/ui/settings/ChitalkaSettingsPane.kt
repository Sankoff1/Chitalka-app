@file:Suppress("LongMethod")

package com.ncorti.kotlin.template.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.chitalka.i18n.APP_LOCALES
import com.chitalka.i18n.AppLocale
import com.chitalka.i18n.I18nUiState
import com.chitalka.i18n.persistLocale
import com.chitalka.library.LastOpenBookPersistence
import com.chitalka.theme.ThemeMode
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
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsCard(
            icon = Icons.Filled.Build,
            title = i18n.t("settings.themeSection"),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    i18n.t("settings.darkTheme"),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
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
        }

        SettingsCard(
            icon = Icons.Filled.Settings,
            title = i18n.t("settings.languageSection"),
        ) {
            LanguageDropdown(
                i18n = i18n,
                selected = locale,
                onSelect = { picked ->
                    scope.launch {
                        persistLocale(persistence, picked)
                        onLocaleChange(picked)
                    }
                },
            )
        }

        SettingsCard(
            icon = Icons.Filled.Info,
            title = i18n.t("settings.versionLabel"),
        ) {
            Text(
                BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LanguageDropdown(
    i18n: I18nUiState,
    selected: AppLocale,
    onSelect: (AppLocale) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val cornerRadius = 12.dp
    val triggerShape = if (expanded) {
        RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        )
    } else {
        RoundedCornerShape(cornerRadius)
    }
    val menuShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = cornerRadius,
        bottomEnd = cornerRadius,
    )
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val belowAnchorPositionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset = IntOffset(anchorBounds.left, anchorBounds.bottom)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidthPx = it.width }
                .clip(triggerShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(width = 1.dp, color = borderColor, shape = triggerShape)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                localeDisplayName(i18n, selected),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = if (expanded) {
                    Icons.Filled.KeyboardArrowUp
                } else {
                    Icons.Filled.KeyboardArrowDown
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded) {
            Popup(
                popupPositionProvider = belowAnchorPositionProvider,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Column(
                    modifier = Modifier
                        .width(with(density) { anchorWidthPx.toDp() })
                        .clip(menuShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(width = 1.dp, color = borderColor, shape = menuShape),
                ) {
                    APP_LOCALES.forEachIndexed { index, loc ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = borderColor,
                            )
                        }
                        val isSelected = loc == selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable {
                                    expanded = false
                                    if (loc != selected) onSelect(loc)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                localeDisplayName(i18n, loc),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun localeDisplayName(i18n: I18nUiState, loc: AppLocale): String =
    when (loc) {
        AppLocale.RU -> i18n.t("settings.languageRu")
        AppLocale.EN -> i18n.t("settings.languageEn")
    }

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
