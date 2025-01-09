package com.shyampatel.githubplayroom.screen.myrepo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.data.github.GithubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MyRepositoriesViewModel(
    private val repository: GithubRepository,
): ViewModel() {

    val myRepositoriesState: StateFlow<MyRepositoriesState> = load().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MyRepositoriesState.Loading,
    )

    private fun load(): Flow<MyRepositoriesState>  {
        return repository.getMyRepositories().map {
            if (it.isSuccess){
                MyRepositoriesState.Success(it.getOrNull()!!)
            } else {
                Log.e(MyRepositoriesViewModel::class.simpleName, it.exceptionOrNull()?.stackTraceToString()?:"")
                MyRepositoriesState.Error
            }
        }
    }
}

sealed interface MyRepositoriesState {
    data class Success(val list: List<GithubRepoModel>) : MyRepositoriesState
    data object Error : MyRepositoriesState
    data object Loading : MyRepositoriesState
}