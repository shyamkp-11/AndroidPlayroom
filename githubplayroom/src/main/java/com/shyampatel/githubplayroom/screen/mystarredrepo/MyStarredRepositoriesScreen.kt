package com.shyampatel.githubplayroom.screen.mystarredrepo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.ui.AndroidPlayroomLoadingIndicator
import com.shyampatel.ui.AndroidPlayroomTopAppBar
import com.shyampatel.githubplayroom.GithubRepoListItem
import com.shyampatel.githubplayroom.R
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun MyStarredRoute(
    modifier: Modifier = Modifier,
    myStarredViewModel: MyStarredViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val starRepositoriesState: MyStarredState by myStarredViewModel.myStarredState.collectAsStateWithLifecycle()
    val starDataLoadedState: StarDataLoadedState by myStarredViewModel.starLoadedState.collectAsStateWithLifecycle()
    MyStarredScreen(
        myStarredState = starRepositoriesState,
        starDataLoadedState = starDataLoadedState,
        onStarClick = { myStarredViewModel.unStar(it) },
        snackBackDismissed = myStarredViewModel::snackbarDismissed,
        onUndoRepoUnStar = myStarredViewModel::undoRepoUnstar,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyStarredScreen(
    myStarredState: MyStarredState,
    starDataLoadedState: StarDataLoadedState,
    onStarClick: (repo: GithubRepoModel) -> Unit,
    snackbarHostState: SnackbarHostState,
    onUndoRepoUnStar: (starredRepo: GithubRepoModel) -> Unit,
    snackBackDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarMessage = stringResource(id = R.string.repository_unstarred)
    val undoMessage = stringResource(id = R.string.undo)
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    LaunchedEffect(starDataLoadedState) {
        if (starDataLoadedState is StarDataLoadedState.ShowUndoUnStarred) {
            val snackBarResult = snackbarHostState.showSnackbar(
                message = snackbarMessage,
                actionLabel = undoMessage,
                duration = SnackbarDuration.Short,
            ) == SnackbarResult.ActionPerformed
            if (snackBarResult) {
                onUndoRepoUnStar(starDataLoadedState.unStarredRepo)
            } else {
                snackBackDismissed()
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
        topBar = {
            AndroidPlayroomTopAppBar(
                titleRes = R.string.starred_repositories,
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        when (myStarredState) {
            is MyStarredState.Success -> {
                LazyColumn(contentPadding = innerPadding, modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection),) {
                    items(
                        items = myStarredState.list,
                        key = { githubRepoModel -> githubRepoModel.id },
                    ) { repo ->
                        GithubRepoListItem(
                            repo = repo,
                            onStarClick = { onStarClick(repo) },
                            uriHandler = LocalUriHandler.current,
                            showStarButton = true,
                            isRepoStarred = true,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }

            MyStarredState.Error -> {}
            MyStarredState.Loading -> {}
        }
        if (myStarredState is MyStarredState.Loading || starDataLoadedState is StarDataLoadedState.Loading) {
            AndroidPlayroomLoadingIndicator(modifier = contentModifier.padding(innerPadding))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyStarredPrev() {
    val model = GithubRepoModel(
        1,
        name = "name",
        fullName = "First Repo",
        stars = 1000,
        ownerId = 1,
        private = false,
        htmlUrl = "",
        ownerLogin = "",
        ownerAvatarUrl = "",
        ownerType = RepoOwnerType.USER,
        description = "description",
        language = "language",
    )
    MyStarredScreen(
        myStarredState = MyStarredState.Success(
            listOf(model)
        ),
        starDataLoadedState = StarDataLoadedState.Loading,
        onStarClick = {},
        snackBackDismissed = {},
        snackbarHostState = remember {
            SnackbarHostState()
        },
        onUndoRepoUnStar = {}
    )
}