package com.shyampatel.githubplayroom.screen.home

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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.githubplayroom.GithubPlayroomTopAppBar
import com.shyampatel.githubplayroom.R
import com.shyampatel.githubplayroom.theme.GithubPlayroomTheme
import com.shyampatel.githubplayroom.theme.HomeIconTintColor
import com.shyampatel.githubplayroom.theme.SigninColor
import com.shyampatel.githubplayroom.theme.SignoutColor
import com.shyampatel.githubplayroom.theme.StarColor
import org.koin.androidx.compose.koinViewModel


@Composable
internal fun HomeScreenRoute(
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onMyRepositoriesClicked: () -> Unit,
    onStarredRepositoriesClicked: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    val homeScreenState: HomeViewModel.HomeState by homeViewModel.isUserAuthenticated.collectAsStateWithLifecycle()
    HomeScreen(
        modifier = modifier,
        onSearchClicked = onSearchClicked,
        onLoginClicked = onLoginClicked,
        onSignoutClicked = homeViewModel::signOut,
        homeScreenState = homeScreenState,
        onMyRepositoriesClicked = onMyRepositoriesClicked,
        onStarredRepositoriesClicked = onStarredRepositoriesClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    homeScreenState: HomeViewModel.HomeState,
    onStarredRepositoriesClicked: () -> Unit,
    onSignoutClicked: () -> Unit,
    onMyRepositoriesClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            GithubPlayroomTopAppBar(
                titleRes = R.string.app_name,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(top = 10.dp)
            ,
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
                            modifier = Modifier,
                            text = "Search Repositories",
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
                                modifier = Modifier,
                                text = "My Repositories",
                                imageVector = Icons.Filled.Code,
                                contentDescription = "My Repositories",
                                iconBackgroundColor = HomeIconTintColor,
                                iconTint = Color.White
                            ) {
                                onMyRepositoriesClicked()
                            }
                            HomeScreenRow(
                                modifier = Modifier,
                                text = "Starred",
                                imageVector = Icons.Sharp.Star,
                                contentDescription = "Starred Repositories",
                                iconBackgroundColor = StarColor,
                                iconTint = Color.White
                            ) {
                                onStarredRepositoriesClicked()
                            }
                            HomeScreenRow(
                                modifier = Modifier,
                                text = "Sign out",
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign out",
                                bottomSpacer = false,
                                iconBackgroundColor = SignoutColor,
                                iconTint = Color.White
                            ) {
                                onSignoutClicked()
                            }
                        }
                    }
                    AnimatedVisibility(visible = homeScreenState == HomeViewModel.HomeState.LoggedOut) {
                        HomeScreenRow(
                            modifier = Modifier,
                            text = "Login",
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
                painterResource(R.drawable.user)
            },
            contentDescription = null,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GithubPlayroomTheme {
        HomeScreen(
            onSearchClicked = {},
            onLoginClicked = {},
            homeScreenState = HomeViewModel.HomeState.LoggedOut,
            onSignoutClicked = {},
            onMyRepositoriesClicked = {},
            onStarredRepositoriesClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewLoggedIn() {
    GithubPlayroomTheme {
        HomeScreen(
            onSearchClicked = {},
            onLoginClicked = {},
            homeScreenState = HomeViewModel.HomeState.LoggedIn(
                RepoOwner(
                    id = 1L,
                    name = "Name",
                    login = "login",
                    avatarUrl = "",
                    htmlUrl = "",
                    type = RepoOwnerType.USER,
                    company = null
                )
            ),
            onSignoutClicked = {},
            onMyRepositoriesClicked = {},
            onStarredRepositoriesClicked = {}
        )
    }
}