package com.shyampatel.githubplayroom.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.data.github.GithubRepository
import kotlinx.coroutines.launch

class GithubLoginViewModel(
    private val repository: GithubRepository
): ViewModel() {

    fun generateToken(code: String, onComplete: ()-> Unit) {
        viewModelScope.launch {
            try {
                repository.generateAccessToken(code)
            } catch (e: Exception) {
                Log.e(GithubLoginViewModel::class.simpleName, e.stackTraceToString())
            }
            onComplete()
        }
    }
}
