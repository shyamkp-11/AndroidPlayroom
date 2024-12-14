package com.shyampatel.withbluetooth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.shyampatel.ui.util.findActivity
import com.shyampatel.withbluetooth.screens.HomeScreenRoute
import kotlinx.serialization.Serializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

fun NavController.navigateToHomeScreen(navOptions: NavOptions? = null) = navigate(
    HomeNavigation, navOptions)

@Serializable
object HomeNavigation

fun NavGraphBuilder.homeScreen() {
    composable<HomeNavigation>(
        deepLinks = listOf(
            navDeepLink<HomeNavigation>(basePath = "$uri/homeScreen")
        )
    ) { backstackEntry ->
        val context = LocalContext.current
        val intent = context.findActivity()?.intent
        var temperature by remember {
            mutableStateOf(intent?.getStringExtra("temperature"))
        }

        HomeScreenRoute(
            autoConnect = (temperature != null),
            onClose = { temperature = null }
        )
    }
}

const val homeScreenIntentExtraKey = "temperature"


