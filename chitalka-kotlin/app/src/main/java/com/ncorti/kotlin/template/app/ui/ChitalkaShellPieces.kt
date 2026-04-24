package com.ncorti.kotlin.template.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chitalka.i18n.I18nUiState
import com.chitalka.library.LibrarySessionState
import com.chitalka.navigation.DrawerNavigationSpec
import com.chitalka.navigation.DrawerScreen
import com.chitalka.navigation.drawerLabelI18nPath

internal fun DrawerScreen.icon(): ImageVector =
    when (this) {
        DrawerScreen.ReadingNow -> Icons.Filled.Home
        DrawerScreen.BooksAndDocs -> Icons.AutoMirrored.Filled.List
        DrawerScreen.Favorites -> Icons.Filled.Favorite
        DrawerScreen.Cart -> Icons.Filled.Delete
        DrawerScreen.DebugLogs -> Icons.Filled.Build
        DrawerScreen.Settings -> Icons.Filled.Settings
    }

@Composable
internal fun ChitalkaDrawerContent(
    selected: DrawerScreen,
    i18n: I18nUiState,
    onSelect: (DrawerScreen) -> Unit,
) {
    ModalDrawerSheet(
        drawerShape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(top = 24.dp, bottom = 12.dp)) {
            DrawerNavigationSpec.drawerScreenOrder.forEach { screen ->
                NavigationDrawerItem(
                    selected = screen == selected,
                    onClick = { onSelect(screen) },
                    icon = {
                        Icon(
                            screen.icon(),
                            contentDescription = null,
                        )
                    },
                    label = { Text(i18n.t(screen.drawerLabelI18nPath)) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(),
                )
            }
        }
    }
}

@Composable
internal fun WelcomeDialog(
    librarySession: LibrarySessionState,
    i18n: I18nUiState,
    onPick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(i18n.t("firstLaunch.message")) },
        text = {
            librarySession.welcomePickerHint?.let { key ->
                Text(
                    i18n.t(key),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onPick) {
                Text(i18n.t("firstLaunch.pickEpub"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(i18n.t("firstLaunch.cancel"))
            }
        },
    )
}
