package com.shyampatel.githubplayroom.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.data.github.GithubRepository
import com.shyampatel.ui.ErrorMessage
import com.shyampatel.githubplayroom.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SearchReposViewModel(
    private val repository: GithubRepository
) : ViewModel() {

    companion object {
        const val TAG = "SearchReposViewModel"
        private const val SEARCH_QUERY_MIN_LENGTH = 2
    }

    private val viewModelState = MutableStateFlow(
        SearchViewModelState(
            isLoading = false,
            errorMessages = emptyList(),
            searchInput = "",
            searchResults = null,
            showUndoStarred = null,
            starredRepo = emptyList()
        )
    )

    val uiState = viewModelState
        .map(SearchViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    private val authenticatedUserToken: StateFlow<String?> = getAuthenticatedUserToken().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    init {
        loadStarredRepo()
    }

    private fun loadStarredRepo() {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    repository.getStarredRepositoriesLiveFlow().map { result ->
                        result.getOrNull() ?: emptyList<String>().also {
                            Log.e(
                                TAG,
                                result.exceptionOrNull()?.stackTraceToString() ?: ""
                            )
                        }
                    }.collect { starredRepo ->
                        viewModelState.update {
                            it.copy(starredRepo = starredRepo)
                        }
                    }
                }
            }
        }
    }
    private fun getAuthenticatedUserToken(): Flow<String?> =
        repository.getUserAccessToken().map { it.getOrNull() }

    private fun getSearchResults(query: String) {
        viewModelState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.searchRepositories(query).map { result ->
                if (result.isSuccess) {
                    viewModelState.value.copy(
                        searchResults = result.getOrNull(),
                        isLoading = false,
                    )

                } else {
                    Log.e(
                        SearchReposViewModel::class.simpleName,
                        result.exceptionOrNull()?.stackTraceToString() ?: ""
                    )
                    viewModelState.value.copy(
                        errorMessages = listOf(
                            ErrorMessage(
                                UUID.randomUUID().mostSignificantBits,
                                R.string.load_error
                            )
                        ),
                        isLoading = false,
                    )
                }
            }.collect { state ->
                viewModelState.update { state }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        viewModelState.update {
            it.copy(searchInput = query)
        }
    }

    fun onSearchTriggered(query: String) {
        if (query.length > SEARCH_QUERY_MIN_LENGTH) {
            viewModelState.update {
                it.copy(searchInput = query)
            }
            getSearchResults(query)
        }
    }

    fun starRepo(repoToStar: GithubRepoModel) {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    viewModelState.update { it.copy(isLoading = true) }
                    val isSuccess = repository.starRepository(token, repoToStar)
                    if (isSuccess.isSuccess) {
                        repository.getRepo(
                            owner = repoToStar.ownerLogin,
                            repo = repoToStar.name
                        ).first().getOrNull()?.let { updatedRepo ->
                            viewModelState.update {
                                it.copy(
                                    searchResults = it.searchResults?.map { githubRepo ->
                                        if (githubRepo.name == repoToStar.name) updatedRepo else githubRepo
                                    },
                                    showUndoStarred = repoToStar,
                                    isLoading = false,
                                )
                            }
                        }
                    } else {
                        Log.e(
                            SearchReposViewModel::class.simpleName,
                            isSuccess.exceptionOrNull()?.stackTraceToString() ?: ""
                        )
                        viewModelState.update {
                            it.copy(
                                errorMessages = listOf(
                                    ErrorMessage(
                                        UUID.randomUUID().mostSignificantBits,
                                        R.string.load_error
                                    )
                                ),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }

    fun undoRepoStar(repoToUnstar: GithubRepoModel) {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    viewModelState.update { it.copy(isLoading = true) }
                    val isSuccess = repository.unstarRepository(token, repoToUnstar)
                    if (isSuccess.isSuccess) {
                        repository.getRepo(
                            owner = repoToUnstar.ownerLogin,
                            repo = repoToUnstar.name
                        ).first().getOrNull()?.let { updatedRepo ->
                            viewModelState.update {
                                it.copy(
                                    searchResults = it.searchResults?.map { githubRepo ->
                                        if (githubRepo.name == repoToUnstar.name) updatedRepo else githubRepo
                                    },
                                    showUndoStarred = null,
                                    isLoading = false,
                                )
                            }
                        }
                    } else {
                        Log.e(
                            SearchReposViewModel::class.simpleName,
                            isSuccess.exceptionOrNull()?.stackTraceToString() ?: ""
                        )
                        viewModelState.update {
                            it.copy(
                                errorMessages = listOf(
                                    ErrorMessage(
                                        UUID.randomUUID().mostSignificantBits,
                                        R.string.load_error
                                    ),
                                ),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }

    fun snackbarDismissed() {
        viewModelState.update {
            it.copy(showUndoStarred = null)
        }
    }
}

sealed interface SearchReposState {
    val isLoading: Boolean
    val errorMessages: List<ErrorMessage>
    val searchInput: String

    data class NoSearchResults(
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val searchInput: String
    ) : SearchReposState

    data class HasSearchResults(
        val searchResults: List<GithubRepoModel>,
        val showUndoStarred: GithubRepoModel?,
        override val isLoading: Boolean,
        override val errorMessages: List<ErrorMessage>,
        override val searchInput: String,
        val starredRepos: List<String>,
    ) : SearchReposState
}

private data class SearchViewModelState(
    val searchResults: List<GithubRepoModel>? = null,
    val isLoading: Boolean = false,
    val errorMessages: List<ErrorMessage> = emptyList(),
    val searchInput: String = "",
    val showUndoStarred: GithubRepoModel?,
    val starredRepo: List<String> = emptyList(),
) {
    fun toUiState(): SearchReposState =
        if (searchResults == null) {
            SearchReposState.NoSearchResults(
                isLoading = isLoading,
                searchInput = searchInput,
                errorMessages = errorMessages
            )
        } else {
            SearchReposState.HasSearchResults(
                searchResults = searchResults,
                errorMessages = errorMessages,
                isLoading = isLoading,
                searchInput = searchInput,
                showUndoStarred = showUndoStarred,
                starredRepos = starredRepo
            )
        }
}