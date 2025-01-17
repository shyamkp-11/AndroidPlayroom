package com.shyampatel.githubplayroom.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.jwt.generateJwtTokenRsa
import com.shyampatel.core.data.github.GithubRepository
import com.shyampatel.githubplayroom.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class GithubLoginViewModel(
    private val repository: GithubRepository
): ViewModel() {

    fun generateToken(code: String, onComplete: (token: String)-> Unit, onError: (e: Exception) -> Unit) {
        viewModelScope.launch {
            try {
                val token = repository.generateAccessToken(code)
                withContext(Dispatchers.Main) {
                    onComplete(token)
                }
            } catch (e: Exception) {
                Log.e(GithubLoginViewModel::class.simpleName, e.stackTraceToString())
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun getAppId() {
        viewModelScope.launch {
            val appId  = repository.getAppId(
                token = generateJwtTokenRsa(
                    issuedAt = Date(Clock.System.now().toEpochMilliseconds()),
                    key = BuildConfig.CLIENT_SECRET,
                    expiry = Date(Clock.System.now().plus(10.minutes).toEpochMilliseconds()),
                    issuer = BuildConfig.CLIENT_GITHUBAPP_ID,
                ),
            )
            withContext(Dispatchers.Main) {
            }
        }
    }
}
