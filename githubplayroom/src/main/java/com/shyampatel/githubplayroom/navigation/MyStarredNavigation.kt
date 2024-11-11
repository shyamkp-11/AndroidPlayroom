package com.shyampatel.githubplayroom.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.githubplayroom.screen.mystarredrepo.MyStarredRoute

const val MY_STARRED_ROUTE = "my_starred_route"
fun NavController.navigateToMyStarred(navOptions: NavOptions? = null) = navigate(
    MY_STARRED_ROUTE, navOptions)

fun NavGraphBuilder.myStarredScreen(
) {
    composable(route = MY_STARRED_ROUTE) {
        MyStarredRoute()
    }
}