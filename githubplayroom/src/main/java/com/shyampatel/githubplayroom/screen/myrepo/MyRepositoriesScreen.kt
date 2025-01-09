package com.shyampatel.githubplayroom.screen.myrepo

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
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
internal fun MyRepositoriesRoute(
    modifier: Modifier = Modifier,
    myRepositoriesViewModel: MyRepositoriesViewModel = koinViewModel(),
) {
    val myRepositoriesState: MyRepositoriesState by myRepositoriesViewModel.myRepositoriesState.collectAsStateWithLifecycle()
    MyRepositoriesScreen(myRepositoriesState = myRepositoriesState, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRepositoriesScreen(
    myRepositoriesState: MyRepositoriesState,
    modifier: Modifier = Modifier,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    Scaffold(
        topBar = {
            AndroidPlayroomTopAppBar(
                titleRes = R.string.my_repositories,
                topAppBarState = topAppBarState
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        val contentModifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        when (myRepositoriesState) {
            is MyRepositoriesState.Success -> {
                LazyColumn(modifier = contentModifier, contentPadding = innerPadding) {
                    items(
                        items = myRepositoriesState.list,
                        key = { githubRepoModel -> githubRepoModel.serverId }
                    ) { task ->
                        GithubRepoListItem(
                            repo = task,
                            showStarButton = false,
                            uriHandler = LocalUriHandler.current,
                            showPublicPrivate = true
                        )
                    }
                }
            }

            MyRepositoriesState.Error -> {}
            MyRepositoriesState.Loading -> {
                AndroidPlayroomLoadingIndicator(modifier = modifier.padding(innerPadding))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun MyRepositoriesPrev() {
    GithubRepoListItem(
        repo = GithubRepoModel(
            name = "name",
            fullName = "First Repo",
            stars = 1000,
            ownerId = "1",
            private = false,
            htmlUrl = "",
            ownerLogin = "",
            ownerAvatarUrl = "",
            ownerType = RepoOwnerType.USER,
            description = "description",
            language = "language",
            serverId = ""
        ), onStarClick = {},
        uriHandler = LocalUriHandler.current
    )
}