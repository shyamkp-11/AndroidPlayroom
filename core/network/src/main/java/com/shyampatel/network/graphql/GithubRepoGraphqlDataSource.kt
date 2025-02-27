package com.shyampatel.network.graphql

import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import kotlinx.coroutines.flow.Flow


interface GithubRepoGraphqlDataSource {
    suspend fun deleteUserAccessToken(token: String): Boolean
    suspend fun getAuthenticatedOwner(token: String): GetAuthenticatedRepoOwnerQuery.Viewer
    suspend fun generateUserAccessToken(code: String): String
    fun getGithubRepo(
        token: String?,
        owner: String,
        repo: String
    ): Flow<GithubRepoModel>

    suspend fun starRepository(token: String, starrableId: String): Boolean
    fun searchRepositories(token: String?, searchQuery: String): Flow<List<GithubRepoModel>>
    fun getOwnerData(
        token: String,
        userLogin: String,
        ownerType: RepoOwnerType
    ): Flow<List<GithubRepoModel>>

    suspend fun unstarRepository(token: String, starrableId: String): Boolean
    fun getStarRepositories(token: String, userLogin: String): Flow<List<GithubRepoModel>>

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

    suspend fun getAppInstallationForUser(token: String, userLogin: String): Result<Unit>
}