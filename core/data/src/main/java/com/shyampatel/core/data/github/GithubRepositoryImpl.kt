package com.shyampatel.core.data.github

import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.database.GithubRepoDao
import com.shyampatel.database.RepoOwnerDao
import com.shyampatel.database.StarredDao
import com.shyampatel.database.StarredEntity
import com.shyampatel.datastore.UserPreferencesDao
import com.shyampatel.network.GithubRepoRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.withContext

class GithubRepositoryImpl(
    private val remoteDataSource: GithubRepoRemoteDataSource,
    private val githubRepoDataSource: GithubRepoDao,
    private val repoOwnerDataSource: RepoOwnerDao,
    private val preferenceDao: UserPreferencesDao,
    private val starredDataSource: StarredDao,
    private val defaultDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher,
) : GithubRepository {
    override fun getMaxStarsGithubRepo(): Flow<Result<List<GithubRepoModel>>> {
        return remoteDataSource.getMaxStarsGithubRepo().map { networkEntities ->
            githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                it.asGithubRepoEntity()
            })
            repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map { networkGithubRepoModel ->
                networkGithubRepoModel.asRepoOwnerEntity()
            })
        }.flatMapLatest {
            githubRepoDataSource.getMaxStarGithubRepoEntities()
        }.map { databaseEntities ->
            Result.success(databaseEntities.map {
                it.asGithubRepoModel()
            })
        }.flowOn(ioDispatcher)
            .catch {
                emit(Result.failure(it))
            }
    }

    override fun getUserAccessToken(): Flow<Result<String?>> {
        return preferenceDao.getUserAccessToken().map {
            Result.success(it)
        }.flowOn(ioDispatcher)
    }

    override fun getAuthenticatedOwner(): Flow<Result<RepoOwner?>> {
        return preferenceDao.getAuthenticatedRepoOwner().map {
            Result.success(it)
        }.flowOn(ioDispatcher)
    }

    override suspend fun generateAccessToken(code: String) {
        val token = remoteDataSource.generateUserAccessToken(code)
        preferenceDao.saveUserAccessToken(token)
        val owner = remoteDataSource.getAuthenticatedOwner(token)
        withContext(ioDispatcher) {
            preferenceDao.saveAuthenticatedOwner(owner.asRepoOwnerEntity().asRepoOwner())
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return withContext(ioDispatcher) {
            val token = preferenceDao.getUserAccessToken().first()
            if (token != null) {
                try {
                    val isSuccessful = remoteDataSource.deleteUserAccessToken(token)
                    preferenceDao.clearAccessToken()
                    preferenceDao.clearAuthenticatedRepoOwner()
                    githubRepoDataSource.deleteAllRepositories()
                    repoOwnerDataSource.deleteRepoOwnerData()
                    starredDataSource.deleteAllStarredData()
                    if (isSuccessful) {
                        return@withContext Result.success(Unit)
                    } else {
                        return@withContext Result.failure(UnknownError())
                    }
                } catch (e: Exception) {
                    return@withContext Result.failure(e)
                }
            } else {
                return@withContext Result.failure(UnknownError("Token is already empty. App in unknown state"))
            }
        }
    }

    override fun getMyRepositories(): Flow<Result<List<GithubRepoModel>>> {
        return getUserAccessToken().zip(
            preferenceDao.getAuthenticatedRepoOwner().map { it?.serverId }) { token, id ->
            Pair(token.getOrNull()!!, id!!)
        }.flatMapLatest { pair ->
            remoteDataSource.getMyRepositories(pair.first).map { networkEntities ->
                githubRepoDataSource.deleteGithubRepoEntitiesForOwner(pair.second)
                githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                    it.asGithubRepoEntity()
                })
                repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map {
                    it.asRepoOwnerEntity()
                })
                return@map pair.second
            }
        }.flatMapLatest {
            githubRepoDataSource.getRepoEntitiesForOwner(it)
        }.map { databaseEntities ->
            Result.success(databaseEntities.map {
                it.asGithubRepoModel()
            })
        }.flowOn(ioDispatcher)
            .catch {
                emit(Result.failure(it))
            }
    }

    override fun getStarredRepositories(): Flow<Result<List<GithubRepoModel>>> {
        return getUserAccessToken().zip(
            preferenceDao.getAuthenticatedRepoOwner().map { it?.serverId }) { token, id ->
            Pair(token.getOrNull()!!, id!!)
        }.flatMapLatest { pair ->
            remoteDataSource.getMyStarredRepositories(pair.first)
                .map { networkEntities ->
                    githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                        it.asGithubRepoEntity()
                    })
                    repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map {
                        it.asRepoOwnerEntity()
                    })
                    starredDataSource.deleteStarredForUser(pair.second)
                    starredDataSource.upsertStarredEntities(networkEntities.map {
                        StarredEntity(userId = pair.second, repoId = it.serverId.toString())
                    })
                    pair.second
                }
        }.flatMapLatest { userId ->
            githubRepoDataSource.getStarredGithubRepoEntitiesForUser(userId)
        }.map { databaseEntities ->
            Result.success(databaseEntities.map {
                it.asGithubRepoModel()
            })
        }.flowOn(ioDispatcher).catch {
            emit(Result.failure(it))
        }
    }

    override fun getStarredRepositoriesLiveFlow(): Flow<Result<List<String>>> {
        return getUserAccessToken().zip(
            preferenceDao.getAuthenticatedRepoOwner().map { it?.serverId }) { token, id ->
            Pair(token.getOrNull()!!, id!!)
        }.flatMapLatest { pair ->
            remoteDataSource.getMyStarredRepositories(pair.first)
                .map { networkEntities ->
                    githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                        it.asGithubRepoEntity()
                    })
                    repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map {
                        it.asRepoOwnerEntity()
                    })
                    starredDataSource.deleteStarredForUser(pair.second)
                    starredDataSource.upsertStarredEntities(networkEntities.map {
                        StarredEntity(userId = pair.second, repoId = it.serverId.toString())
                    })
                    pair.second
                }
        }.flatMapLatest { userId ->
            starredDataSource.getStarredEntitiesForUsers(listOf(userId))
        }.map { databaseEntities ->
            Result.success(databaseEntities.map {
                it.repoId
            })
        }.flowOn(ioDispatcher).catch {
            emit(Result.failure(it))
        }
    }

    override fun searchRepositories(searchQuery: String): Flow<Result<List<GithubRepoModel>>> {
        return remoteDataSource.searchRepositories(searchQuery).map { networkEntities ->
            networkEntities.map { it.asGithubRepoEntity().asGithubRepoModel() }
        }.map {
            Result.success(it)
        }.catch {
            emit(Result.failure(exception = it))
        }
    }

    override fun getRepo(owner: String, repo: String): Flow<Result<GithubRepoModel>> {
        return preferenceDao.getUserAccessToken().flatMapLatest {
            remoteDataSource.getGithubRepo(token = it?.ifEmpty { null }, owner = owner, repo = repo)
        }.map {
            Result.success(it.asGithubRepoEntity().asGithubRepoModel())
        }.catch {
            emit(Result.failure(exception = it))
        }
    }

    override suspend fun starRepository(
        token: String,
        githubRepoModel: GithubRepoModel
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val isSuccessful: Boolean = remoteDataSource.starRepository(
                    owner = githubRepoModel.ownerLogin,
                    repo = githubRepoModel.name,
                    token = token,
                )
                if (isSuccessful) {
                    // insert to db
                    val userId = preferenceDao.getAuthenticatedRepoOwner().first()?.serverId
                    starredDataSource.insertOrIgnoreStarredEntities(
                        listOf(
                            StarredEntity(
                                repoId = githubRepoModel.serverId,
                                userId = userId!!
                            )
                        )
                    )
                    return@withContext Result.success(Unit)
                } else {
                    return@withContext Result.failure(UnknownError())
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    override suspend fun unstarRepository(
        token: String, githubRepoModel: GithubRepoModel
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val isSuccessful: Boolean = remoteDataSource.unstarRepository(
                    owner = githubRepoModel.ownerLogin,
                    repo = githubRepoModel.name,
                    token = token,
                )
                if (isSuccessful) {
                    val userId = preferenceDao.getAuthenticatedRepoOwner().first()?.serverId
                    starredDataSource.deleteStarredEntities(
                        listOf(
                            StarredEntity(
                                repoId = githubRepoModel.serverId,
                                userId = userId!!
                            )
                        )
                    )
                    return@withContext Result.success(Unit)
                } else {
                    return@withContext Result.failure(UnknownError())
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }
}
