package com.shyampatel.githubplayroom.screen.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.ui.AndroidPlayroomLoadingIndicator
import com.shyampatel.githubplayroom.GithubRepoListItem
import com.shyampatel.githubplayroom.R
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SearchRepositoriesRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    searchReposViewModel: SearchReposViewModel = koinViewModel(),
) {
    val searchReposState: SearchReposState by searchReposViewModel.uiState.collectAsStateWithLifecycle()
    SearchRepositoriesScreen(
        modifier = modifier,
        searchReposState = searchReposState,
        onSearchQueryChanged = searchReposViewModel::onSearchQueryChanged,
        onBackClick = onBackClick,
        onSearchTriggered = searchReposViewModel::onSearchTriggered,
        onStarRepo = searchReposViewModel::starRepo,
        onUndoStarRepo = searchReposViewModel::undoRepoStar,
        snackBackDismissed = searchReposViewModel::snackbarDismissed,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRepositoriesScreen(
    modifier: Modifier = Modifier,
    searchReposState: SearchReposState,
    onStarRepo: (GithubRepoModel) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    onSearchTriggered: (String) -> Unit = {},
    onBackClick: () -> Unit,
    onUndoStarRepo: (starredRepo: GithubRepoModel) -> Unit,
    snackBackDismissed: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val snackbarMessage = stringResource(id = R.string.repository_starred)
    val undoMessage = stringResource(id = R.string.undo)

    Scaffold(
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)},
        modifier = modifier,
    ) { innerPadding ->

        if (searchReposState is SearchReposState.HasSearchResults) {
            LaunchedEffect(searchReposState.showUndoStarred) {
                if (searchReposState.showUndoStarred != null) {
                    val snackBarResult = snackbarHostState.showSnackbar(
                        message = snackbarMessage,
                        actionLabel = undoMessage,
                        duration = SnackbarDuration.Short,
                    ) == SnackbarResult.ActionPerformed
                    if (snackBarResult) {
                        onUndoStarRepo(searchReposState.showUndoStarred)
                    } else {
                        snackBackDismissed()
                    }
                }
            }
        }
        Box(modifier = modifier.padding(top = innerPadding.calculateTopPadding())) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchToolbar(
                    onBackClick = onBackClick,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onSearchTriggered = onSearchTriggered,
                    searchQuery = searchReposState.searchInput,
                )
                when (searchReposState) {
                    is SearchReposState.HasSearchResults -> {
                        searchReposState.searchResults.let {
                            LazyColumn(modifier = Modifier, contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())) {
                                items(
                                    items = it,
                                    key = { githubRepoModel -> githubRepoModel.id }
                                ) { repo ->
                                    GithubRepoListItem(
                                        repo = repo,
                                        onStarClick = { onStarRepo(repo) },
                                        isRepoStarred = searchReposState.starredRepos.contains(repo.id),
                                        uriHandler = LocalUriHandler.current
                                    )
                                }
                            }
                        }
                    }
                    is SearchReposState.NoSearchResults -> {

                    }
                }
            }
            if (searchReposState.isLoading) {
                AndroidPlayroomLoadingIndicator(modifier = modifier)
            }
        }
    }
}

@Composable
private fun SearchToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                contentDescription = stringResource(
                    id = R.string.back,
                ),
            )
        }
        SearchTextField(
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchTriggered = onSearchTriggered,
            searchQuery = searchQuery,
        )
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchTriggered: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSearchExplicitlyTriggered = {
        keyboardController?.hide()
        onSearchTriggered(searchQuery)
    }

    TextField(
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Sharp.Search,
                contentDescription = stringResource(
                    id = R.string.feature_search_title,
                ),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchQueryChanged("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Close,
                        contentDescription = stringResource(
                            id = R.string.feature_search_clear_search_text_content_desc,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        onValueChange = {
            if ("\n" !in it) onSearchQueryChanged(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    onSearchExplicitlyTriggered()
                    true
                } else {
                    false
                }
            },
        shape = RoundedCornerShape(32.dp),
        value = searchQuery,
        placeholder = { Text(stringResource(id = R.string.search_repositories)) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchExplicitlyTriggered()
            },
        ),
        maxLines = 1,
        singleLine = true,
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Preview(showBackground = true)
@Composable
fun SearchRepositoriesScreenPreview() {
    AndroidPlayroomTheme {
        SearchRepositoriesScreen(
            onStarRepo = {},
            onSearchQueryChanged = {},
            onSearchTriggered = {},
            onBackClick = {},
            onUndoStarRepo = {},
            snackBackDismissed = {},
            snackbarHostState = remember {SnackbarHostState()},
            searchReposState = SearchReposState.HasSearchResults (
                searchResults = listOf(
                    GithubRepoModel(
                        1,
                        name = "name 1",
                        fullName = "First Repo",
                        stars = 1000,
                        ownerId = 1,
                        private = false,
                        htmlUrl = "",
                        ownerLogin = "",
                        ownerAvatarUrl = "",
                        ownerType = RepoOwnerType.USER,
                        description = "description 1",
                        language = "language 1"
                    ),
                    GithubRepoModel(
                        2,
                        fullName = "Second Repo",
                        name = "name 2",
                        stars = 1200,
                        ownerId = 2,
                        private = true,
                        htmlUrl = "",
                        ownerLogin = "",
                        ownerAvatarUrl = "",
                        ownerType = RepoOwnerType.USER,
                        description = "description 2",
                        language = "language 2",
                    ),
                ),
                showUndoStarred = null,
                isLoading = false,
                errorMessages = emptyList(),
                starredRepos = emptyList(),
                searchInput = "",
            )
        )
    }
}