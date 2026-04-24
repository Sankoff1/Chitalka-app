@file:Suppress("LongParameterList")

package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chitalka.i18n.I18nUiState
import com.chitalka.navigation.DrawerScreen
import com.chitalka.navigation.drawerLabelI18nPath
import com.chitalka.ui.topbar.AppTopBarSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChitalkaTopBar(
    selected: DrawerScreen,
    i18n: I18nUiState,
    searchChrome: AppTopBarSpec.SearchChromeState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onClearQuery: () -> Unit,
) {
    val showSearchInput = AppTopBarSpec.shouldShowSearchInput(selected.routeName, searchChrome)
    TopAppBar(
        title = {
            if (showSearchInput) {
                CompactSearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = i18n.t(AppTopBarSpec.I18nKeys.SEARCH_PLACEHOLDER),
                    showClear = AppTopBarSpec.shouldShowClearQueryButton(
                        selected.routeName,
                        searchChrome,
                    ),
                    onClear = onClearQuery,
                )
            } else {
                Text(
                    i18n.t(selected.drawerLabelI18nPath),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        navigationIcon = {
            if (showSearchInput) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = i18n.t(AppTopBarSpec.I18nKeys.A11Y_OPEN_MENU),
                    )
                }
            } else {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = i18n.t(AppTopBarSpec.I18nKeys.A11Y_OPEN_MENU),
                    )
                }
            }
        },
        actions = {
            if (!showSearchInput &&
                AppTopBarSpec.shouldShowSearchButton(selected.routeName, searchChrome)
            ) {
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = i18n.t(AppTopBarSpec.I18nKeys.SEARCH_PLACEHOLDER),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun CompactSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    showClear: Boolean,
    onClear: () -> Unit,
) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(onPrimary.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = onPrimary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onPrimary.copy(alpha = 0.7f),
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = onPrimary,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
                cursorBrush = SolidColor(onPrimary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showClear) {
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = placeholder,
                    tint = onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
