package com.shyampatel.githubplayroom.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.data.GithubRepository
import kotlinx.coroutines.launch

class GithubLoginViewModel(
    private val repository: GithubRepository
): ViewModel() {

    fun generateToken(code: String, onComplete: ()-> Unit) {
        viewModelScope.launch {
            repository.generateAccessToken(code)
            onComplete()
        }
    }
}
