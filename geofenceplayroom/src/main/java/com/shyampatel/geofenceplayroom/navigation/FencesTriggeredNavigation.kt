package com.shyampatel.geofenceplayroom.navigation

import androidx.compose.material3.DrawerState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.shyampatel.geofenceplayroom.screen.fencetriggered.FenceTriggeredScreenRoute
import kotlinx.serialization.Serializable

fun NavController.navigateToGeofenceTriggeredScreen(id: Long, navOptions: NavOptions? = null) {
    navigate(
        route = FencesTriggeredNavigation(id.toString()),
        navOptions = navOptions,
    )
}

@Serializable
data class FencesTriggeredNavigation(val id: String)

fun NavGraphBuilder.geofenceTriggeredScreen(
    drawerState: DrawerState,
    topBarBackIconClick: () -> Unit,
) {
    composable<FencesTriggeredNavigation>(
        deepLinks = listOf(
            navDeepLink<FencesTriggeredNavigation>(basePath = "$uri/geofences")
        )
    ){backStackEntry ->
        FenceTriggeredScreenRoute(
            topBarBackIconClick = topBarBackIconClick,
            id = backStackEntry.toRoute<FencesTriggeredNavigation>().id.toLong()
        )
    }
}