package com.shyampatel.githubplayroom.screen.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.data.GithubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchReposViewModel(
    private val repository: GithubRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchRepoState: MutableStateFlow<SearchReposUiState> by lazy {
        MutableStateFlow(
            SearchReposUiState.EmptyQuery
        )
    }
    val searchRepoState: StateFlow<SearchReposUiState> = _searchRepoState

    private val _searchRepoDataLoadedState: MutableStateFlow<SearchReposDataLoadedState> by lazy {
        MutableStateFlow(
            SearchReposDataLoadedState.Init
        )
    }
    val searchRepoDataLoadedState: StateFlow<SearchReposDataLoadedState> =
        _searchRepoDataLoadedState

    private val _myStarredRepo: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList())
    val myStarredRepo: StateFlow<List<Long>> = _myStarredRepo

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
                        result.getOrNull() ?: emptyList<Long>().also {
                            Log.e(
                                SearchReposViewModel::class.simpleName,
                                result.exceptionOrNull()?.stackTraceToString() ?: ""
                            )
                        }
                    }.collect{ starredRepo ->
                        _myStarredRepo.value = starredRepo
                    }
                }
            }
        }
    }

    private var lastQuery: String = ""
    private var searchResultRepos: List<GithubRepoModel>? = null

    private fun getAuthenticatedUserToken(): Flow<String?> = repository.getUserAccessToken().map { it.getOrNull() }

    private fun getSearchResults(query: String) {
        _searchRepoState.value = SearchReposUiState.Loading
        viewModelScope.launch {
            repository.searchRepositories(query).map {
                if (it.isSuccess) {
                    searchResultRepos = it.getOrNull()
                    SearchReposUiState.Success(searchResultRepos!!)
                } else {
                    Log.e(
                        SearchReposViewModel::class.simpleName,
                        it.exceptionOrNull()?.stackTraceToString() ?: ""
                    )
                    SearchReposUiState.Error
                }
            }.collect {
                _searchRepoState.value = it
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchTriggered(query: String) {
        if (query.length > SEARCH_QUERY_MIN_LENGTH) {
            lastQuery = query
            getSearchResults(query)
        }
    }

    fun starRepo(repoToStar: GithubRepoModel) {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    _searchRepoDataLoadedState.value = SearchReposDataLoadedState.Loading
                    val isSuccess = repository.starRepository(token, repoToStar)
                    if (isSuccess.isSuccess) {
                        repository.getRepo(
                            owner = repoToStar.ownerLogin,
                            repo = repoToStar.name
                        ).first().getOrNull()?.let { updatedRepo ->
                            searchResultRepos = searchResultRepos!!.map {
                                if (it.name == repoToStar.name) updatedRepo else it
                            }
                            _searchRepoState.value =
                                SearchReposUiState.Success(searchResultRepos!!)
                        }
                    } else {
                        Log.e(
                            SearchReposViewModel::class.simpleName,
                            isSuccess.exceptionOrNull()?.stackTraceToString() ?: ""
                        )
                        _searchRepoDataLoadedState.value = SearchReposDataLoadedState.Error
                    }
                    _searchRepoDataLoadedState.value =
                        SearchReposDataLoadedState.ShowUndoStarred(starredRepo = repoToStar)
                }
            }
        }
    }

    fun undoRepoStar(repoToUnstar: GithubRepoModel) {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    _searchRepoDataLoadedState.value = SearchReposDataLoadedState.Loading
                    val isSuccess = repository.unstarRepository(token, repoToUnstar)
                    if (isSuccess.isSuccess) {
                        repository.getRepo(
                            owner = repoToUnstar.ownerLogin,
                            repo = repoToUnstar.name
                        ).first().getOrNull()?.let { updatedRepo ->
                            searchResultRepos = searchResultRepos!!.map {
                                if (it.name == repoToUnstar.name) updatedRepo else it
                            }
                            _searchRepoState.value =
                                SearchReposUiState.Success(searchResultRepos!!)
                        }
                        _searchRepoDataLoadedState.value = SearchReposDataLoadedState.LoadingFinish
                    } else {
                        Log.e(
                            SearchReposViewModel::class.simpleName,
                            isSuccess.exceptionOrNull()?.stackTraceToString() ?: ""
                        )
                        _searchRepoDataLoadedState.value = SearchReposDataLoadedState.Error
                    }

                }
            }
        }
    }

    fun snackbarDismissed() {
        _searchRepoDataLoadedState.value =
            SearchReposDataLoadedState.UndoStarFinished
    }
}

sealed interface SearchReposUiState {
    open class Success(open val list: List<GithubRepoModel>) : SearchReposUiState
    data object Error : SearchReposUiState
    data object Loading : SearchReposUiState
    data object EmptyQuery : SearchReposUiState
}

sealed interface SearchReposDataLoadedState {
    data object Init : SearchReposDataLoadedState
    data object Loading : SearchReposDataLoadedState
    data object LoadingFinish : SearchReposDataLoadedState
    data class ShowUndoStarred(val starredRepo: GithubRepoModel) : SearchReposDataLoadedState
    data object UndoStarFinished : SearchReposDataLoadedState
    data object EmptyQuery : SearchReposDataLoadedState
    data object Error : SearchReposDataLoadedState
}

private const val SEARCH_QUERY_MIN_LENGTH = 2