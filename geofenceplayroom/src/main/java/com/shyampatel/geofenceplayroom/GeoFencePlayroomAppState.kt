package com.shyampatel.geofenceplayroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.shyampatel.geofenceplayroom.navigation.FencesTriggeredNavigation
import com.shyampatel.geofenceplayroom.navigation.HomeNavigation
import com.shyampatel.geofenceplayroom.navigation.TopLevelDestination
import com.shyampatel.geofenceplayroom.navigation.ViewGeofencesNavigation
import com.shyampatel.geofenceplayroom.navigation.navigateToHome
import com.shyampatel.geofenceplayroom.navigation.navigateToViewGeofences
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberGeoFencePlayroomAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): GeoFencePlayroomAppState {
    val selectedTopLevelDestination: MutableState<TopLevelDestination?> = rememberSaveable(navController.currentBackStackEntry) {
        mutableStateOf(null)
    }

    return remember(
        navController
    ) {
        GeoFencePlayroomAppState(
            selectedTopLevelDestination = selectedTopLevelDestination,
            navController = navController,
            coroutineScope = coroutineScope,
        )
    }
}

@Stable
class GeoFencePlayroomAppState(
    val selectedTopLevelDestination: MutableState<TopLevelDestination?>,
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
) {
    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail. The key is the
     * route.
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route?.substringBefore("/")) {
            HomeNavigation::class.qualifiedName -> TopLevelDestination.HOME
            ViewGeofencesNavigation::class.qualifiedName -> TopLevelDestination.VIEW_GEOFENCES
            FencesTriggeredNavigation::class.qualifiedName -> TopLevelDestination.VIEW_GEOFENCES
            else -> null
        }

    val shouldShowNavDrawer: Boolean
        get() = true

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        when (topLevelDestination) {
            TopLevelDestination.HOME -> {
                navController.navigateToHome(topLevelNavOptions)
            }
            TopLevelDestination.VIEW_GEOFENCES -> {
                navController.navigateToViewGeofences(topLevelNavOptions)
            }
        }

    }
}