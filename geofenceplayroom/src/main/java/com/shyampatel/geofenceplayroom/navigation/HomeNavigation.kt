package com.shyampatel.geofenceplayroom.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.geofenceplayroom.screen.HomeScreenRoute

const val HOME_ROUTE = "home_route"
fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(
    HOME_ROUTE, navOptions)

fun NavGraphBuilder.homeScreen() {
    composable(route = HOME_ROUTE) {
        HomeScreenRoute()
    }
}