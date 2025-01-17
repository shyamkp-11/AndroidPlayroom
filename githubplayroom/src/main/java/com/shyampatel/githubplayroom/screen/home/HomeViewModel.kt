package com.shyampatel.githubplayroom.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.data.github.GithubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    // Todo find a better design for this
    private val _loggedInLoading = MutableStateFlow(false)
    val loggedInLoading: StateFlow<Boolean> = _loggedInLoading

    private fun isUserAuthenticated(): Flow<HomeState> {
        return repository.getUserAccessToken()
            .zip(repository.getAuthenticatedOwner()) { token, owner -> Pair(token, owner) }.
            zip(repository.getNotificationEnabled()) { pair, notificationsEnabled -> Triple(pair.first, pair.second, notificationsEnabled) }
                .map {
            val token = it.first.getOrNull()
            val authenticatedOwner = it.second.getOrNull()
            if (token.isNullOrEmpty() || authenticatedOwner == null) {
                HomeState.LoggedOut
            } else {
                HomeState.LoggedIn(authenticatedOwner = authenticatedOwner, it.third.getOrNull() ?: false)
            }
        }
    }

    fun toggleNotifications() {
        if(!loggedInLoading.value) {
            isUserAuthenticated.value.let {
                viewModelScope.launch {
                    _loggedInLoading.value = true
                    if (it is HomeState.LoggedIn) {
                        repository.setNotificationEnabled(!it.notificationsEnabled)
                    }
                    _loggedInLoading.value = false
                }
            }
        }
    }

    fun signOut(deleteCookieData: suspend ()-> Unit) {
        if(!loggedInLoading.value) {
            viewModelScope.launch {
                repository.signOut()
                deleteCookieData()
            }
        }
    }

    sealed interface HomeState {
        class LoggedIn(val authenticatedOwner: RepoOwner, val notificationsEnabled: Boolean) : HomeState
        data object LoggedOut : HomeState
        data object Error : HomeState
        data object Loading : HomeState
    }
}
