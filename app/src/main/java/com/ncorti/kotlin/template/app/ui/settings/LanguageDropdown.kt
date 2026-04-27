package com.ncorti.kotlin.template.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val DROPDOWN_CORNER_RADIUS = 12.dp
private val DROPDOWN_BORDER_WIDTH = 1.dp
private const val DROPDOWN_BORDER_ALPHA = 0.4f
private const val SELECTED_ROW_BG_ALPHA = 0.12f
private val DROPDOWN_ROW_PADDING_H = 16.dp
private val DROPDOWN_ROW_PADDING_V = 14.dp

@Composable
internal fun LanguageDropdown(
    i18n: I18nUiState,
    selected: AppLocale,
    onSelect: (AppLocale) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val triggerShape =
        if (expanded) {
            RoundedCornerShape(
                topStart = DROPDOWN_CORNER_RADIUS,
                topEnd = DROPDOWN_CORNER_RADIUS,
                bottomStart = 0.dp,
                bottomEnd = 0.dp,
            )
        } else {
            RoundedCornerShape(DROPDOWN_CORNER_RADIUS)
        }
    val menuShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = DROPDOWN_CORNER_RADIUS,
        bottomEnd = DROPDOWN_CORNER_RADIUS,
    )
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = DROPDOWN_BORDER_ALPHA)
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
                .border(width = DROPDOWN_BORDER_WIDTH, color = borderColor, shape = triggerShape)
                .clickable { expanded = !expanded }
                .padding(horizontal = DROPDOWN_ROW_PADDING_H, vertical = DROPDOWN_ROW_PADDING_V),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                localeDisplayName(i18n, selected),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
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
                        .border(width = DROPDOWN_BORDER_WIDTH, color = borderColor, shape = menuShape),
                ) {
                    APP_LOCALES.forEachIndexed { index, loc ->
                        if (index > 0) HorizontalDivider(color = borderColor)
                        LocaleRow(
                            i18n = i18n,
                            loc = loc,
                            isSelected = loc == selected,
                            onPick = {
                                expanded = false
                                if (loc != selected) onSelect(loc)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocaleRow(
    i18n: I18nUiState,
    loc: AppLocale,
    isSelected: Boolean,
    onPick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_ROW_BG_ALPHA),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onPick)
            .padding(horizontal = DROPDOWN_ROW_PADDING_H, vertical = DROPDOWN_ROW_PADDING_V),
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

private fun localeDisplayName(i18n: I18nUiState, loc: AppLocale): String =
    when (loc) {
        AppLocale.RU -> i18n.t("settings.languageRu")
        AppLocale.EN -> i18n.t("settings.languageEn")
    }
