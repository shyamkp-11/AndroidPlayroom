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
import java.util.Date
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

    fun hasUserInstalledTheApp(onComplete: (success: Boolean)-> Unit) {
        viewModelScope.launch {
            try {
                val issuedAt = Date(Clock.System.now().toEpochMilliseconds())
                val expiredAt = Date(Clock.System.now().plus(9.minutes).toEpochMilliseconds())
                val result = repository.getAppInstallations(
                    token = generateJwtTokenRsa(
                        issuedAt = issuedAt,
                        key = BuildConfig.JWT_KEY,
                        expiry = expiredAt,
                        issuer = BuildConfig.CLIENT_ID_GITHUBAPP,
                    )
                )
                Log.d(GithubLoginViewModel::class.simpleName, "getAppId: $result")
                withContext(Dispatchers.Main) {
                    onComplete(result.isSuccess)
                }
            } catch (e: Exception) {
                Log.e(GithubLoginViewModel::class.simpleName, e.stackTraceToString())
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
}
