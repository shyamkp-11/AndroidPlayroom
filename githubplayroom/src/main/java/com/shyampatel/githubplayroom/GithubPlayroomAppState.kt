package com.shyampatel.githubplayroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shyampatel.githubplayroom.navigation.GithubPlayroomDestination
import com.shyampatel.githubplayroom.navigation.GithubPlayroomDestination.HOME
import com.shyampatel.githubplayroom.navigation.GithubPlayroomDestination.MY_REPOSITORIES
import com.shyampatel.githubplayroom.navigation.GithubPlayroomDestination.MY_STARRED
import com.shyampatel.githubplayroom.navigation.GithubPlayroomDestination.SEARCH
import com.shyampatel.githubplayroom.navigation.HOME_ROUTE
import com.shyampatel.githubplayroom.navigation.MY_REPOSITORIES_ROUTE
import com.shyampatel.githubplayroom.navigation.MY_STARRED_ROUTE
import com.shyampatel.githubplayroom.navigation.SEARCH_REPOS_ROUTE

@Composable
fun rememberGithubPlayroomAppState(
    navController: NavHostController = rememberNavController(),
): GithubPlayroomAppState {
    return remember {
        GithubPlayroomAppState(
            navController = navController
        )
    }
}

@Stable
class GithubPlayroomAppState(
    val navController: NavHostController,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentGithubPlayroomDestination: GithubPlayroomDestination?
        @Composable get() = when (currentDestination?.route) {
            SEARCH_REPOS_ROUTE -> SEARCH
            MY_REPOSITORIES_ROUTE -> MY_REPOSITORIES
            HOME_ROUTE -> HOME
            MY_STARRED_ROUTE -> MY_STARRED
            else -> null
        }

    val shouldShowTopBar: Boolean
        @Composable
        get() = currentDestination?.route != SEARCH_REPOS_ROUTE

}