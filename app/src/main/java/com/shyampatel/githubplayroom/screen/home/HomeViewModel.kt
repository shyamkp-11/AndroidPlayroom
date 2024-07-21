package com.shyampatel.githubplayroom.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.data.GithubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: GithubRepository
): ViewModel() {

    val isUserAuthenticated: StateFlow<HomeState> = isUserAuthenticated().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState.Loading,
    )

    private fun isUserAuthenticated(): Flow<HomeState> {
        return repository.getUserAccessToken()
            .zip(repository.getAuthenticatedOwner()) { token, owner -> Pair(token, owner) }.map {
            val token = it.first.getOrNull()
            val authenticatedOwner = it.second.getOrNull()
            if (token.isNullOrEmpty() || authenticatedOwner == null) {
                HomeState.LoggedOut
            } else {
                HomeState.LoggedIn(authenticatedOwner = authenticatedOwner)
            }
        }
    }

    fun signOut(deleteCookieData: suspend ()-> Unit) {
        viewModelScope.launch {
            repository.signOut()
            deleteCookieData()
        }
    }

    sealed interface HomeState {
        data class LoggedIn(val authenticatedOwner: RepoOwner) : HomeState
        data object LoggedOut : HomeState
        data object Error : HomeState
        data object Loading : HomeState
    }
}
