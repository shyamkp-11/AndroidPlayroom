package com.shyampatel.withbluetooth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberWithBluetoothAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): WithBluetoothAppState {

    return remember(
        navController
    ) {
        WithBluetoothAppState(
            navController = navController,
            coroutineScope = coroutineScope,
        )
    }
}

@Stable
class WithBluetoothAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
) {

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

}