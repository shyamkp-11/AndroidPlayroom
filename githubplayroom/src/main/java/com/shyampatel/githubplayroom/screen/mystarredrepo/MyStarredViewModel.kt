package com.shyampatel.githubplayroom.screen.mystarredrepo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.data.github.GithubRepository
import com.shyampatel.githubplayroom.screen.search.SearchReposViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyStarredViewModel(
    private val repository: GithubRepository,
) : ViewModel() {

    val myStarredState: StateFlow<MyStarredState> = load().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MyStarredState.Loading,
    )

    private val _starLoadedState: MutableStateFlow<StarDataLoadedState> =
        MutableStateFlow(StarDataLoadedState.Init)
    val starLoadedState: StateFlow<StarDataLoadedState> = _starLoadedState

    private fun load() =
        repository.getStarredRepositories()
            .map {
                if (it.isSuccess) {
                    MyStarredState.Success(it.getOrNull()!!)
                } else {
                    Log.e(
                        SearchReposViewModel::class.simpleName,
                        it.exceptionOrNull()?.stackTraceToString() ?: ""
                    )
                    MyStarredState.Error
                }
            }


    private val authenticatedUserToken: StateFlow<String?> = getAuthenticatedUserToken().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    private fun getAuthenticatedUserToken(): Flow<String?> = repository.getUserAccessToken().map { it.getOrNull() }


    fun undoRepoUnstar(repoToStar: GithubRepoModel) {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    _starLoadedState.update { StarDataLoadedState.Loading }
                    val isSuccess = repository.starRepository(token, repoToStar)
                    if (isSuccess.isSuccess) {
                        repository.getRepo(
                            owner = repoToStar.ownerLogin,
                            repo = repoToStar.name
                        ).first().getOrNull()?.let { _ ->
                        }
                        _starLoadedState.update { StarDataLoadedState.LoadingFinish }
                    } else {
                        Log.e(
                            SearchReposViewModel::class.simpleName,
                            isSuccess.exceptionOrNull()?.stackTraceToString() ?: ""
                        )
                        _starLoadedState.update { StarDataLoadedState.Error }
                    }
                }
            }
        }
    }

    fun unStar(starredRepo: GithubRepoModel) {
        viewModelScope.launch {
            authenticatedUserToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    _starLoadedState.value = StarDataLoadedState.Loading

                    val isSuccess = repository.unstarRepository(token, starredRepo)
                    if (isSuccess.isSuccess) {
                        _starLoadedState.value =
                            StarDataLoadedState.ShowUndoUnStarred(unStarredRepo = starredRepo)
                    } else {
                        Log.e(
                            SearchReposViewModel::class.simpleName,
                            isSuccess.exceptionOrNull()?.stackTraceToString() ?: ""
                        )
                        _starLoadedState.update { StarDataLoadedState.Error }
                    }
                    // state will updated from the flow in init
                }
            }
        }
    }

    fun snackbarDismissed() {
        _starLoadedState.update { StarDataLoadedState.UndoUnStarFinished }
    }
}


sealed interface MyStarredState {
    data class Success(val list: List<GithubRepoModel>) : MyStarredState
    data object Error : MyStarredState
    data object Loading : MyStarredState
}

sealed interface StarDataLoadedState {
    data object Error : StarDataLoadedState
    data object Loading : StarDataLoadedState
    data object Init : StarDataLoadedState
    data object LoadingFinish : StarDataLoadedState
    data class ShowUndoUnStarred(val unStarredRepo: GithubRepoModel) : StarDataLoadedState
    data object UndoUnStarFinished : StarDataLoadedState
}