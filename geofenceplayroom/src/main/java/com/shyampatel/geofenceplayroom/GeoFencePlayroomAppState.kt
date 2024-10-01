package com.shyampatel.geofenceplayroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberGeoFencePlayroomAppState(
    navController: NavHostController = rememberNavController(),
): GeoFencePlayroomAppState {
    return remember {
        GeoFencePlayroomAppState(
            navController = navController
        )
    }
}

@Stable
class GeoFencePlayroomAppState(
    val navController: NavHostController,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination
}