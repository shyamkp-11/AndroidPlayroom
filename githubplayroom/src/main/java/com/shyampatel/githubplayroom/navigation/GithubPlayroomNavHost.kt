package com.shyampatel.githubplayroom.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun GithubPlayroomNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
        startDestination = HomeNavigation,
        modifier = modifier
    ) {

        permissionsScreenNotification(
            onGranted = {
                navController.previousBackStackEntry?.savedStateHandle?.set(ENABLE_NOTIFICATIONS_KEY, true)
                navController.popBackStack()
                        },
            onCancelButtonClicked = navController::popBackStack,
        )
        homeScreen(
            onLoginClicked = navController::navigateToGithubLoginWebview,
            onMaxStarReposClicked = navController::navigateToSearch,
            onMyRepositoriesClicked = navController::navigateToMyRepositories,
            onStarredRepositoriesClicked = navController::navigateToMyStarred,
            navigateToPermissionsScreen = navController::navigateNotificationPermissions
        )
        searchReposScreen(
            onBackClick = navController::popBackStack,
            modifier = modifier,
        )
        githubLoginWebViewScreen(onFinish = {navController.popBackStack()})
        myRepositoriesScreen()
        myStarredScreen()
    }
}