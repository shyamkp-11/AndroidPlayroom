package com.shyampatel.datastore

import com.shyampatel.core.common.RepoOwner
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDao {
    fun getUserAccessToken(): Flow<String?>
    suspend fun saveUserAccessToken(token: String)
    suspend fun clearAccessToken()
    suspend fun saveAuthenticatedOwner(owner: RepoOwner)
    fun getAuthenticatedRepoOwner(): Flow<RepoOwner?>
    suspend fun clearAuthenticatedRepoOwner()
}