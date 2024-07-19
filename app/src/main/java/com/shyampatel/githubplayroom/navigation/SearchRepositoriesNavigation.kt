package com.shyampatel.githubplayroom.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.githubplayroom.screen.search.SearchRepositoriesRoute

const val SEARCH_REPOS_ROUTE = "search_repos_route"
fun NavController.navigateToSearch(navOptions: NavOptions? = null) = navigate(
    SEARCH_REPOS_ROUTE, navOptions)

fun NavGraphBuilder.searchReposScreen(
    onBackClick: () -> Unit,
    modifier: Modifier,
) {
    composable(route = SEARCH_REPOS_ROUTE) {
        BackHandler(true) {
            onBackClick.invoke()
        }
        SearchRepositoriesRoute(onBackClick = onBackClick, modifier = modifier)
    }
}