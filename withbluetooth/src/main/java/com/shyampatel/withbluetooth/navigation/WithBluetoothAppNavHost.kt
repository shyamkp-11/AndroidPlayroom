package com.shyampatel.withbluetooth.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.shyampatel.withbluetooth.WithBluetoothAppState
import kotlin.reflect.KClass

@Composable
fun WithBluetoothAppNavHost(
    appState: WithBluetoothAppState,
    startDestination: KClass<*>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {
    NavHost(
        navController = navController,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(700)
            )
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
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(700)
            )
        },
        startDestination = startDestination,
        modifier = modifier
    ) {
        homeScreen()
    }
}

const val uri = "https://www.ishyampatel.com/withbluetooth"