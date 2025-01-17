package com.shyampatel.datastore

import com.shyampatel.core.common.RepoOwner
import kotlinx.coroutines.flow.Flow

interface UserPreferencesDao {
    fun getUserAccessToken(): Flow<String?>
    suspend fun saveUserAccessToken(token: String): String
    suspend fun clearAccessToken()
    suspend fun saveAuthenticatedOwner(owner: RepoOwner): RepoOwner
    fun getAuthenticatedRepoOwner(): Flow<RepoOwner?>
    suspend fun clearAuthenticatedRepoOwner()
    suspend fun saveNotificationEnabled(enabled: Boolean): Boolean
    fun getNotificationEnabled(): Flow<Boolean?>
    suspend fun clearNotificationPreference()
    suspend fun saveFCMToken(token: String): String
    fun getFCMToken(): Flow<String?>
    suspend fun clearFCMToken()
    suspend fun clearFid()
    fun getFid(): Flow<String?>
    suspend fun saveFid(fid: String): String
}