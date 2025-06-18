package com.shyampatel.githubplayroom

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.shyampatel.githubplayroom.navigation.GithubPlayroomNavHost
import com.shyampatel.githubplayroom.navigation.HomeNavigation
import com.shyampatel.githubplayroom.navigation.SEARCH_REPOS_ROUTE
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FirstRunTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            GithubPlayroomNavHost(navController = navController)
        }
    }

    @Test
    fun sampleTest() {
        composeTestRule.onNodeWithText("Search Repositories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Starred").assertDoesNotExist()
        composeTestRule.onAllNodes(hasTestTag(TestingConstant.HOME_SCREEN_MENU_ITEM)).assertCountEquals(2)
        Assert.assertEquals(HomeNavigation::class.qualifiedName, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navigateToSearchScreen() {
        composeTestRule.onNodeWithText("Search Repositories").performClick()
        Assert.assertEquals(SEARCH_REPOS_ROUTE, navController.currentBackStackEntry?.destination?.route)
    }
}