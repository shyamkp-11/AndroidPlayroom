package com.shyampatel.network

import kotlinx.coroutines.flow.Flow


interface GithubRepoRemoteDataSource {
    fun getMaxStarsGithubRepo(): Flow<List<NetworkGithubRepoModel>>
    suspend fun deleteUserAccessToken(token: String): Boolean
    suspend fun getAuthenticatedOwner(token: String): NetworkRepoOwner
    suspend fun generateUserAccessToken(code: String): String
    fun getMyRepositories(token: String): Flow<List<NetworkGithubRepoModel>>
    fun getMyStarredRepositories(token: String): Flow<List<NetworkGithubRepoModel>>
    fun searchRepositories(searchQuery: String): Flow<List<NetworkGithubRepoModel>>
    suspend fun starRepository(token: String, owner: String, repo: String): Boolean
    suspend fun unstarRepository(token: String, owner: String, repo: String): Boolean
    fun getGithubRepo(
        token: String?,
        owner: String,
        repo: String
    ): Flow<NetworkGithubRepoModel>

    suspend fun appServerSignedInToApp(
        fcmToken: String,
        deviceId: String,
        globalId: String,
        userLogin: String,
        firstName: String,
        lastName: String,
        email: String,
    ): Boolean

    suspend fun appServerSaveNotificationEnabled(
        globalId: String,
        deviceId: String,
        fcmEnabled: Boolean,
        fcmToken: String
    ): Boolean

    suspend fun appServerNotifySignOut(
        globalId: String,
        deviceId: String,
        fcmToken: String
    ): Boolean

    suspend fun appServerSaveFcmToken(globalId: String, deviceId: String, fcmToken: String): Boolean
}