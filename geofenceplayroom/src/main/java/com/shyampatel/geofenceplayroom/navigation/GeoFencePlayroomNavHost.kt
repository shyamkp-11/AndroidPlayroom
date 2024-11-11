package com.shyampatel.geofenceplayroom.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.shyampatel.geofenceplayroom.GeoFencePlayroomAppState
import com.shyampatel.geofenceplayroom.navigation.permissions.PermissionNotificationNavigation
import com.shyampatel.geofenceplayroom.navigation.permissions.navigateBackgroundLocationPermissions
import com.shyampatel.geofenceplayroom.navigation.permissions.navigateNotificationPermissions
import com.shyampatel.geofenceplayroom.navigation.permissions.permissionsScreenBackgroundLocation
import com.shyampatel.geofenceplayroom.navigation.permissions.permissionsScreenForegroundLocation
import com.shyampatel.geofenceplayroom.navigation.permissions.permissionsScreenNotification
import kotlin.reflect.KClass

@Composable
fun GeoFencePlayroomNavHost(
    appState: GeoFencePlayroomAppState,
    startDestination: KClass<*>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    drawerState: DrawerState,
) {
    NavHost(
        navController = navController,
        enterTransition = {
            if (targetState.destination.route == PermissionNotificationNavigation::class.qualifiedName) {
                slideInVertically(
                    initialOffsetY = { fullHeight -> 3 * fullHeight / 2 },
                    animationSpec = tween(700)
                )
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(700)
                )
            }
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(700)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(700)
            )
        },
        popExitTransition = {
            if (initialState.destination.route == PermissionNotificationNavigation::class.qualifiedName) {
                slideOutVertically (
                    targetOffsetY = { fullHeight -> 3*fullHeight/2 },
                    animationSpec = tween(700)
                )
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(700)
                )
            }
        },
        startDestination = startDestination,
        modifier = modifier
    ) {

        permissionsScreenForegroundLocation(
            onGranted = {
                navController.popBackStack()
                navController.navigateBackgroundLocationPermissions()
            }
        )

        permissionsScreenBackgroundLocation(
            onGranted = {
                navController.popBackStack()
                navController.navigateToHome()
            }
        )

        permissionsScreenNotification(
            onGranted = navController::popBackStack,
            onCancelButtonClicked = navController::popBackStack,
        )

        homeScreen(
            drawerState = drawerState,
            navigateToViewGeofences = { appState.navigateToTopLevelDestination(TopLevelDestination.VIEW_GEOFENCES) },
        )

        viewGeofencesScreen(
            drawerState = drawerState,
            navigateToFenceTriggered = navController::navigateToGeofenceTriggeredScreen,
            navigateToPermissionsScreen = navController::navigateNotificationPermissions,
        )

        geofenceTriggeredScreen(
            drawerState = drawerState,
            topBarBackIconClick = navController::popBackStack,
        )
    }
}

const val uri = "https://www.ishyampatel.com"