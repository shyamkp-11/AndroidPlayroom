package com.shyampatel.githubplayroom.navigation

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.shyampatel.githubplayroom.screen.login.GithubWebViewRoute

const val GITHUB_LOGIN_WEBVIEW = "github_login_webview"
fun NavController.navigateToGithubLoginWebview(navOptions: NavOptions? = null) = navigate(
    GITHUB_LOGIN_WEBVIEW, navOptions)

fun NavGraphBuilder.githubLoginWebViewScreen(
    onFinish:(success: Boolean)->Unit
) {
    composable(route = GITHUB_LOGIN_WEBVIEW) {
        BackHandler(true) {
        }
        GithubWebViewRoute(onFinish = onFinish)
    }
}