package com.shyampatel.geofenceplayroom.navigation

import androidx.compose.material3.DrawerState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.geofenceplayroom.screen.home.HomeScreenRoute
import kotlinx.serialization.Serializable

const val HOME_ROUTE = "home_route"
fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(
    HomeNavigation, navOptions)

@Serializable
object HomeNavigation

fun NavGraphBuilder.homeScreen(drawerState: DrawerState, navigateToViewGeofences: () -> Unit) {
    composable<HomeNavigation> { backstackEntry ->
        HomeScreenRoute(
            drawerState = drawerState,
            navigateToViewGeofences = navigateToViewGeofences
        )
    }
}