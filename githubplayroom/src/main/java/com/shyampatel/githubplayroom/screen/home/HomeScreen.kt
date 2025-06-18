package com.shyampatel.githubplayroom.screen.home

import android.content.Intent
import android.webkit.CookieManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.messaging.FirebaseMessaging
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.githubplayroom.BuildConfig
import com.shyampatel.ui.AndroidPlayroomTopAppBar
import com.shyampatel.githubplayroom.R
import com.shyampatel.githubplayroom.TestingConstant
import com.shyampatel.githubplayroom.screen.login.AuthenticationActivity
import com.shyampatel.githubplayroom.screen.login.AuthenticationActivity.Companion.KEY_TOKEN
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import com.shyampatel.ui.theme.HomeIconTintColor
import com.shyampatel.ui.theme.SigninColor
import com.shyampatel.ui.theme.SignoutColor
import com.shyampatel.ui.theme.StarColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@Composable
internal fun HomeScreenRoute(
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onMyRepositoriesClicked: () -> Unit,
    onStarredRepositoriesClicked: () -> Unit,
    navigateToPermissionsScreen: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
    enableNotificationsIfNot: Boolean,
) {
//    LaunchedEffect(key1 = Unit) {
//        FirebaseMessaging.getInstance().deleteToken()
//    }
    val context = LocalContext.current
    val homeScreenState: HomeViewModel.HomeState by homeViewModel.isUserAuthenticated.collectAsStateWithLifecycle()
    val loggedInLoading: Boolean by homeViewModel.loggedInLoading.collectAsStateWithLifecycle()
    val result = remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result.value = it.data?.getStringExtra(KEY_TOKEN)
        }

    LaunchedEffect(key1 = enableNotificationsIfNot) {
        if (homeScreenState is HomeViewModel.HomeState.LoggedIn) {
            if (enableNotificationsIfNot && !(homeScreenState as HomeViewModel.HomeState.LoggedIn).notificationsEnabled) {
                homeViewModel.toggleNotifications()
            }
        }
    }

    HomeScreen(
        modifier = modifier,
        onSearchClicked = onSearchClicked,
        onLoginClicked = {
            if (BuildConfig.WEB_AUTHENTICATION_MODE == "CHROME_CUSTOM_TABS") {
                launcher.launch(Intent(context, AuthenticationActivity::class.java))
            } else {
                onLoginClicked()
            }
        },
        onSignoutClicked = homeViewModel::signOut,
        homeScreenState = homeScreenState,
        onMyRepositoriesClicked = onMyRepositoriesClicked,
        onStarredRepositoriesClicked = onStarredRepositoriesClicked,
        onNotificationsToggled = homeViewModel::toggleNotifications,
        loggedInLoading = loggedInLoading,
        navigateToPermissionsScreen = navigateToPermissionsScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    homeScreenState: HomeViewModel.HomeState,
    onStarredRepositoriesClicked: () -> Unit,
    onNotificationsToggled: () -> Unit,
    onSignoutClicked: (deleteCookieData: suspend () -> Unit) -> Unit,
    onMyRepositoriesClicked: () -> Unit,
    loggedInLoading: Boolean,
    navigateToPermissionsScreen: () -> Unit,
) {
    Scaffold(
        topBar = {
            AndroidPlayroomTopAppBar(
                titleRes = R.string.app_name,
            )
        },
    ) { innerPadding ->

        val notificationPermissionGranted = rememberPermissionState(permission = "android.permission.POST_NOTIFICATIONS")

            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(top = 10.dp),
            ) {
                AnimatedVisibility(homeScreenState is HomeViewModel.HomeState.LoggedIn) {
                    if (homeScreenState is HomeViewModel.HomeState.LoggedIn) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        ) {
                            OwnerProfileImage(
                                repoOwnerImageUrl = homeScreenState.authenticatedOwner.avatarUrl,
                                cropCircle = homeScreenState.authenticatedOwner.type == RepoOwnerType.USER
                            )
                            Column(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = homeScreenState.authenticatedOwner.name ?: "",
                                    modifier = Modifier,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = homeScreenState.authenticatedOwner.login,
                                    modifier = modifier,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier.padding(
                        top = 24.dp
                    ),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    onSearchClicked()
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            HomeScreenRow(
                                modifier = Modifier.testTag(TestingConstant.HOME_SCREEN_MENU_ITEM),
                                text = stringResource(id = R.string.search_repositories),
                                imageVector = Icons.Sharp.Search,
                                contentDescription = "Search repositories",
                                iconBackgroundColor = HomeIconTintColor,
                                iconTint = Color.White
                            ) {
                                onSearchClicked()
                            }
                        }
                        AnimatedVisibility(visible = homeScreenState is HomeViewModel.HomeState.LoggedIn) {
                            Column {
                                HomeScreenRow(
                                    modifier = Modifier.testTag(TestingConstant.HOME_SCREEN_MENU_ITEM),
                                    text = stringResource(R.string.my_repositories),
                                    imageVector = Icons.Filled.Code,
                                    contentDescription = "My Repositories",
                                    iconBackgroundColor = HomeIconTintColor,
                                    iconTint = Color.White
                                ) {
                                    onMyRepositoriesClicked()
                                }
                                HomeScreenRow(
                                    modifier = Modifier.testTag(TestingConstant.HOME_SCREEN_MENU_ITEM),
                                    text = stringResource(R.string.starred),
                                    imageVector = Icons.Sharp.Star,
                                    contentDescription = "Starred Repositories",
                                    iconBackgroundColor = StarColor,
                                    iconTint = Color.White
                                ) {
                                    onStarredRepositoriesClicked()
                                }
                                if (homeScreenState is HomeViewModel.HomeState.LoggedIn) {
                                    HomeScreenRow(
                                        modifier = Modifier.testTag(TestingConstant.HOME_SCREEN_MENU_ITEM),
                                        text = if (homeScreenState.notificationsEnabled) stringResource(
                                            R.string.notifications_on
                                        ) else stringResource(R.string.notifications_off),
                                        imageVector = if (homeScreenState.notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = "Enable / Disable Notifications",
                                        iconBackgroundColor = if (homeScreenState.notificationsEnabled) SigninColor else SignoutColor,
                                        iconTint = Color.White
                                    ) {
                                        if (!homeScreenState.notificationsEnabled && !notificationPermissionGranted.status.isGranted) {
                                            navigateToPermissionsScreen()
                                        } else {
                                            onNotificationsToggled()
                                        }
                                    }
                                }
                                HomeScreenRow(
                                    modifier = Modifier.testTag(TestingConstant.HOME_SCREEN_MENU_ITEM),
                                    text = stringResource(R.string.sign_out),
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    bottomSpacer = false,
                                    iconBackgroundColor = SignoutColor,
                                    iconTint = Color.White
                                ) {
                                    onSignoutClicked {
                                        withContext(Dispatchers.Default) {
                                            CookieManager.getInstance().removeAllCookies(null);
                                            CookieManager.getInstance().flush();
                                        }
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(visible = homeScreenState == HomeViewModel.HomeState.LoggedOut) {
                            HomeScreenRow(
                                modifier = Modifier.testTag(TestingConstant.HOME_SCREEN_MENU_ITEM),
                                text = stringResource(R.string.login),
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = "Login",
                                bottomSpacer = false,
                                iconBackgroundColor = SigninColor,
                                iconTint = Color.White
                            ) {
                                onLoginClicked()
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun HomeScreenSpacer(modifier: Modifier) {
    Spacer(
        modifier = modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun HomeScreenRow(
    modifier: Modifier,
    text: String,
    imageVector: ImageVector, contentDescription: String,
    bottomSpacer: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconBackgroundColor: Color = Color.Transparent,
    onListItemClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .clickable {
                onListItemClick()
            },
    ) {
        Box(modifier = Modifier
            .padding(start = 10.dp)
            .background(
                color = iconBackgroundColor,
                shape = RoundedCornerShape(5.dp)
            )
            .align(Alignment.CenterVertically)
            .padding(4.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )
            }
            if (bottomSpacer) {
                HomeScreenSpacer(modifier)
            }
        }
    }
}

@Composable
fun OwnerProfileImage(
    cropCircle: Boolean = false,
    repoOwnerImageUrl: String?,
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = repoOwnerImageUrl,
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            isError = state is AsyncImagePainter.State.Error
        },
    )
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = Modifier
            .size(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            // Display a progress bar while loading
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        Image(
            modifier = if (cropCircle) {
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            } else {
                Modifier
                    .fillMaxSize()
            },
            contentScale = ContentScale.Crop,
            painter = if (isError.not() && !isLocalInspection) {
                imageLoader
            } else {
                painterResource(com.shyampatel.ui.R.drawable.user)
            },
            contentDescription = null,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidPlayroomTheme {
        HomeScreen(
            onSearchClicked = {},
            onLoginClicked = {},
            homeScreenState = HomeViewModel.HomeState.LoggedOut,
            onSignoutClicked = {},
            onMyRepositoriesClicked = {},
            onStarredRepositoriesClicked = {},
            onNotificationsToggled = {},
            loggedInLoading = false,
            navigateToPermissionsScreen = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewLoggedIn() {
    AndroidPlayroomTheme {
        HomeScreen(
            onSearchClicked = {},
            onLoginClicked = {},
            homeScreenState = HomeViewModel.HomeState.LoggedIn(
                RepoOwner(
                    name = "Name",
                    login = "login",
                    avatarUrl = "",
                    htmlUrl = "",
                    type = RepoOwnerType.USER,
                    company = null,
                    serverId = ""
                ),
                notificationsEnabled = true
            ),
            onSignoutClicked = {},
            onMyRepositoriesClicked = {},
            onStarredRepositoriesClicked = {},
            onNotificationsToggled = {},
            loggedInLoading = false,
            navigateToPermissionsScreen = {},
        )
    }
}