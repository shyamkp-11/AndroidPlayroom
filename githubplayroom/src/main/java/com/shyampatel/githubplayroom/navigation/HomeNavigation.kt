package com.shyampatel.githubplayroom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.shyampatel.githubplayroom.screen.home.HomeScreenRoute
import kotlinx.serialization.Serializable

const val HOME_ROUTE = "home_route"
const val ENABLE_NOTIFICATIONS_KEY = "ENABLE_NOTIFICATIONS"
fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(
    HomeNavigation, navOptions)

@Serializable
object HomeNavigation

fun NavGraphBuilder.homeScreen(
    onMaxStarReposClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onMyRepositoriesClicked: () -> Unit,
    onStarredRepositoriesClicked: () -> Unit,
    navigateToPermissionsScreen: () -> Unit,
) {
    composable<HomeNavigation> { backStackEntry ->
        val enableNotificationsIfNot = backStackEntry.savedStateHandle.getLiveData<Boolean>(ENABLE_NOTIFICATIONS_KEY).observeAsState()
        enableNotificationsIfNot.value?.let {
            backStackEntry.savedStateHandle.remove<Boolean>(ENABLE_NOTIFICATIONS_KEY)
        }
        HomeScreenRoute(
            enableNotificationsIfNot = enableNotificationsIfNot.value ?: false,
            onSearchClicked = onMaxStarReposClicked,
            onLoginClicked = onLoginClicked,
            onMyRepositoriesClicked = onMyRepositoriesClicked,
            onStarredRepositoriesClicked = onStarredRepositoriesClicked,
            navigateToPermissionsScreen = navigateToPermissionsScreen
        )
    }
}