package com.shyampatel.geofenceplayroom.navigation

import androidx.compose.material3.DrawerState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.geofenceplayroom.screen.viewgeofences.ViewGeofencesScreenRoute
import kotlinx.serialization.Serializable

const val VIEW_GEOFENCES_ROUTE = "view_geofences_route"
fun NavController.navigateToViewGeofences(navOptions: NavOptions? = null) {
    navigate(
        route = ViewGeofencesNavigation,
        navOptions = navOptions
    )
}

@Serializable
object ViewGeofencesNavigation

fun NavGraphBuilder.viewGeofencesScreen(drawerState: DrawerState,
                                        navigateToFenceTriggered: (id: Long) -> Unit,
                                        navigateToPermissionsScreen: () -> Unit) {
    composable<ViewGeofencesNavigation>(
    ) {
        ViewGeofencesScreenRoute(
            drawerState = drawerState,
            navigateToFenceTriggered = navigateToFenceTriggered,
            navigateToPermissionsScreen = navigateToPermissionsScreen
        )
    }
}