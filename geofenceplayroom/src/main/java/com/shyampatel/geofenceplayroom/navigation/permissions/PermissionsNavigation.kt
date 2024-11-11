package com.shyampatel.geofenceplayroom.navigation.permissions

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.geofenceplayroom.screen.permissions.PermissionsScreen
import kotlinx.serialization.Serializable

const val PERMISSIONS_ROUTE = "permissions_route"
fun NavController.navigateForegroundLocationPermissions(navOptions: NavOptions? = null) {
    navigate(
        route = PermissionsForegroundLocationNavigation,
        navOptions = navOptions,
    )
}
fun NavController.navigateBackgroundLocationPermissions(navOptions: NavOptions? = null) {
    navigate(
        route = PermissionsBackgroundLocationNavigation,
        navOptions = navOptions,
    )
}
fun NavController.navigateNotificationPermissions(navOptions: NavOptions? = null) {
    navigate(
        route = PermissionNotificationNavigation,
        navOptions = navOptions,
    )
}

@Serializable
object PermissionsForegroundLocationNavigation
@Serializable
object PermissionsBackgroundLocationNavigation
@Serializable
object PermissionNotificationNavigation


fun NavGraphBuilder.permissionsScreenForegroundLocation(
    onGranted: () -> Unit, ) {
    composable<PermissionsForegroundLocationNavigation>() { backStackEntry ->
        PermissionsScreen(
            permissions = setOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            requiredPermissions = setOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            title = """Set to "Allow While Using App" """,
            description = """GeofencePlayroom collects location data to determine the user's location to trigger appropriate geofence.""",
            requiredTitle = "You will need to set Location permissions to \"Allow All the time\" in Settings. If not, this app will not work correctly",
            requiredDescription = """1. Click the below "Open Settings"
                    |2. Click "Permission" and then "Location"
                    |3. Set to "Allow all the time".
                """.trimMargin(),
            onGranted = onGranted
        )
    }
}


fun NavGraphBuilder.permissionsScreenBackgroundLocation(
    onGranted: () -> Unit, ) {
    composable<PermissionsBackgroundLocationNavigation>() { backStackEntry ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionsScreen(
                permissions = setOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                ),
                requiredPermissions = setOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                ),
                title = """Need Permission for background location. Please set "Allow all the time" in Settings""",
                description = """Please set Location permission to "Allow all the time" in Settings. """,
                requiredTitle = "You will need to set Location permissions to \"Allow All the time\" in Settings. If not, this app will not work correctly",
                requiredDescription = """1. Click the below "Open Settings"
                        |2. Click "Permission" and then "Location"
                        |3. Set to "Allow all the time".
                    """.trimMargin(),
                onGranted = {
                    onGranted()
                }
            )
        }
    }
}

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
            )
        }
    }
}