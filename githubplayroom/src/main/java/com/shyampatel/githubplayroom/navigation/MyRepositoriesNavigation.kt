package com.shyampatel.githubplayroom.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.githubplayroom.screen.myrepo.MyRepositoriesRoute

const val MY_REPOSITORIES_ROUTE = "my_repositories_route"
fun NavController.navigateToMyRepositories(navOptions: NavOptions? = null) = navigate(
    MY_REPOSITORIES_ROUTE, navOptions)

fun NavGraphBuilder.myRepositoriesScreen() {
    composable(route = MY_REPOSITORIES_ROUTE) {
        MyRepositoriesRoute()
    }
}