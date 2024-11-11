package com.shyampatel.githubplayroom.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.githubplayroom.screen.home.HomeScreenRoute

const val HOME_ROUTE = "home_route"
fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(
    HOME_ROUTE, navOptions)

fun NavGraphBuilder.homeScreen(
    onMaxStarReposClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onMyRepositoriesClicked: () -> Unit,
    onStarredRepositoriesClicked: () -> Unit,
) {
    composable(route = HOME_ROUTE) {
        HomeScreenRoute(
            onSearchClicked = onMaxStarReposClicked,
            onLoginClicked = onLoginClicked,
            onMyRepositoriesClicked = onMyRepositoriesClicked,
            onStarredRepositoriesClicked = onStarredRepositoriesClicked
        )
    }
}