package com.shyampatel.core.data.github

import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwner
import kotlinx.coroutines.flow.Flow

interface GithubRepository {
    fun getMaxStarsGithubRepo(): Flow<Result<List<GithubRepoModel>>>
    fun getUserAccessToken(): Flow<Result<String?>>
    suspend fun generateAccessToken(code: String)
    suspend fun signOut(): Result<Unit>
    fun getAuthenticatedOwner(): Flow<Result<RepoOwner?>>
    fun getMyRepositories(): Flow<Result<List<GithubRepoModel>>>
    fun getStarredRepositories(): Flow<Result<List<GithubRepoModel>>>
    fun getStarredRepositoriesLiveFlow(): Flow<Result<List<String>>>
    suspend fun starRepository(token: String, githubRepoModel: GithubRepoModel): Result<Unit>
    suspend fun unstarRepository(token: String, githubRepoModel: GithubRepoModel): Result<Unit>
    fun getRepo(owner: String, repo: String): Flow<Result<GithubRepoModel>>
    fun searchRepositories(searchQuery: String): Flow<Result<List<GithubRepoModel>>>
}