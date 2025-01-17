package com.shyampatel.githubplayroom.navigation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.shyampatel.ui.permissions.PermissionsScreen
import com.shyampatel.ui.util.dp
import kotlinx.serialization.Serializable

fun NavController.navigateNotificationPermissions(navOptions: NavOptions? = null) {
    navigate(
        route = PermissionNotificationNavigation,
        navOptions = navOptions,
    )
}

@Serializable
object PermissionNotificationNavigation

fun NavGraphBuilder.permissionsScreenNotification(
    onGranted: () -> Unit,
    onCancelButtonClicked: () -> Unit,
){
    composable<PermissionNotificationNavigation> { backStackEntry ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionsScreen(
                permissions = setOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
                requiredPermissions = setOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
                onGranted = {
                    onGranted()
                },
                title = "Allow Notifications",
                description = "Allow Geofence App to notify you when a geofence event occurs",
                cancelText = "Not Now",
                onCancelButtonClicked = {
                    onCancelButtonClicked()
                },
                settingsText = "Settings",
                openSettingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, LocalContext.current.packageName),
                modifier =  Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            )
        }
    }
}