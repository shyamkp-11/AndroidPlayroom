package com.shyampatel.geofenceplayroom.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.shyampatel.geofenceplayroom.R
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
    val route: KClass<*>
) {
    HOME(
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        iconTextId = R.string.home,
        titleTextId = R.string.home,
        route = HomeNavigation::class
    ),
    VIEW_GEOFENCES(
        selectedIcon = Icons.Filled.PinDrop,
        unselectedIcon = Icons.Outlined.PinDrop,
        iconTextId = R.string.view_geofences_screen_top_bar_title,
        titleTextId = R.string.view_geofences_screen_top_bar_title,
        route = ViewGeofencesNavigation::class
    )
}