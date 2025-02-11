package com.shyampatel.core.data.github.graphql

import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.core.data.github.GithubRepository
import com.shyampatel.core.data.github.asGithubRepoEntity
import com.shyampatel.core.data.github.asGithubRepoModel
import com.shyampatel.core.data.github.asRepoOwnerEntity
import com.shyampatel.database.GithubRepoDao
import com.shyampatel.database.RepoOwnerDao
import com.shyampatel.database.StarredDao
import com.shyampatel.database.StarredEntity
import com.shyampatel.datastore.UserPreferencesDao
import com.shyampatel.network.graphql.GithubRepoGraphqlDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.withContext

class GithubRepositoryImpl(
    private val remoteDataSource: GithubRepoGraphqlDataSource,
    private val githubRepoDataSource: GithubRepoDao,
    private val repoOwnerDataSource: RepoOwnerDao,
    private val preferenceDao: UserPreferencesDao,
    private val starredDataSource: StarredDao,
    private val defaultDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher,
) : GithubRepository {
    override fun getMaxStarsGithubRepo(): Flow<Result<List<GithubRepoModel>>> {
        TODO("")
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

    override suspend fun generateAccessToken(code: String): String {
        return withContext(ioDispatcher) {
            val token = remoteDataSource.generateUserAccessToken(code)
            preferenceDao.saveUserAccessToken(token)
            val owner = remoteDataSource.getAuthenticatedOwner(token)
            preferenceDao.getFCMToken().zip(preferenceDao.getFid()) { fcmToken, fid ->
                runCatching {
                    remoteDataSource.appServerSignedInToApp(
                        userLogin = owner.login,
                        deviceId = fid!!,
                        globalId = owner.id,
                        fcmToken = fcmToken!!,
                        firstName = owner.name ?: "",
                        lastName = "",
                        email = /*owner.email ?:*/ ""
                    ).let { fcmEnabled ->
                        preferenceDao.saveNotificationEnabled(fcmEnabled)
                    }
                }
            }.first()
            preferenceDao.saveAuthenticatedOwner(RepoOwner(
                serverId = owner.id,
                login = owner.login,
                htmlUrl = owner.url.toString(),
                type = RepoOwnerType.valueOf(owner.__typename.uppercase()),
                avatarUrl = owner.avatarUrl.toString(),
                name = owner.name,
                company = owner.company
            ))
            return@withContext token
        }
    }

    override suspend fun getAppInstallations(token: String): Result<Unit> {
        return preferenceDao.getAuthenticatedRepoOwner().first().let {
            remoteDataSource.getAppInstallationForUser(
                    token = token,
                    userLogin = it!!.login
            )
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return withContext(ioDispatcher) {
            val token = preferenceDao.getUserAccessToken().first()
            if (token != null) {
                try {
                    val owner = preferenceDao.getAuthenticatedRepoOwner().first()!!
                    val isSuccessful = remoteDataSource.deleteUserAccessToken(token)
                    preferenceDao.getFCMToken().zip(preferenceDao.getFid()) { fcmToken, fid ->
                        remoteDataSource.appServerNotifySignOut(
                            globalId = owner.serverId,
                            deviceId = fid!!,
                            fcmToken = fcmToken!!
                        )
                    }.first()
                    preferenceDao.clearAccessToken()
                    preferenceDao.clearAuthenticatedRepoOwner()
                    preferenceDao.clearNotificationPreference()
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
            preferenceDao.getAuthenticatedRepoOwner().map { it }) { token, user  ->
            Pair(token.getOrThrow()!!, user!!)
        }.flatMapLatest { pair ->
            remoteDataSource.getOwnerData(token = pair.first, userLogin = pair.second.login, ownerType = pair.second.type).map { networkEntities ->
                githubRepoDataSource.deleteGithubRepoEntitiesForOwner(pair.second.serverId)
                githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                    it.asGithubRepoEntity()
                })
                repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map {
                    it.asRepoOwnerEntity()
                })
                return@map pair.second.serverId
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
            preferenceDao.getAuthenticatedRepoOwner().map { it?.serverId to it?.login }) { token, (id, login) ->
            Triple(token.getOrThrow()!!, id!!, login!!)
        }.flatMapLatest { pair ->
            remoteDataSource.getStarRepositories(token = pair.first, userLogin = pair.third)
                .map { networkEntities ->
                    githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                        it.asGithubRepoEntity()
                    })
                    repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map {
                        it.asRepoOwnerEntity()
                    })
                    starredDataSource.deleteStarredForUser(pair.second)
                    starredDataSource.upsertStarredEntities(networkEntities.map {
                        StarredEntity(userId = pair.second, repoId = it.serverId)
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
            preferenceDao.getAuthenticatedRepoOwner().map { it?.serverId to it?.login }) { token, (id, login) ->
            Triple(token.getOrThrow()!!, id!!, login!!)
        }.flatMapLatest { pair ->
            remoteDataSource.getStarRepositories(token = pair.first, userLogin = pair.third)
                .map { networkEntities ->
                    githubRepoDataSource.upsertGithubRepoEntities(networkEntities.map {
                        it.asGithubRepoEntity()
                    })
                    repoOwnerDataSource.upsertRepoOwnerEntities(networkEntities.map {
                        it.asRepoOwnerEntity()
                    })
                    starredDataSource.deleteStarredForUser(pair.second)
                    starredDataSource.upsertStarredEntities(networkEntities.map {
                        StarredEntity(userId = pair.second, repoId = it.serverId)
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
        return getUserAccessToken().zip(
            preferenceDao.getAuthenticatedRepoOwner().map { it?.serverId to it?.login }) { token, (id, login) ->
            Triple(token.getOrNull(), id, login)
        }.flatMapLatest {triple ->
             remoteDataSource.searchRepositories(token = triple.first, searchQuery = searchQuery)
                .map { networkEntities ->
                    networkEntities.map { it.asGithubRepoEntity().asGithubRepoModel() }
                }.map {
                Result.success(it)
            }.catch {
                emit(Result.failure(exception = it))
            }
        }
    }

    override suspend fun setNotificationEnabled(enabled: Boolean): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val owner = preferenceDao.getAuthenticatedRepoOwner().first()!!
                val remoteSuccess = preferenceDao.getFCMToken().zip(preferenceDao.getFid()) { fcmToken, fid ->
                    remoteDataSource.appServerSaveNotificationEnabled(
                        globalId = owner.serverId,
                        deviceId = fid!!,
                        fcmEnabled = enabled,
                        fcmToken = fcmToken!!
                    )
                }.first()
                if (remoteSuccess) {
                    preferenceDao.saveNotificationEnabled(enabled)
                } else {
                    return@withContext Result.failure(UnknownError())
                }
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    override fun getNotificationEnabled(): Flow<Result<Boolean>> {
        return preferenceDao.getNotificationEnabled().map {
            val enabled = it ?: false
            Result.success(enabled)
        }.flowOn(ioDispatcher).catch {
            emit(Result.failure(it))
        }
    }

    override suspend fun saveFcmToken(token: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                preferenceDao.saveFCMToken(token)
                val owner = preferenceDao.getAuthenticatedRepoOwner().first()
                if (owner != null) {
                    remoteDataSource.appServerSaveFcmToken(
                        globalId = owner.serverId,
                        deviceId = preferenceDao.getFid().first()!!,
                        fcmToken = token)
                }
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    override fun getFid(): Flow<Result<String>> {
        return preferenceDao.getFid().map {
            if (it != null) {
                return@map Result.success(it)
            } else {
                return@map Result.failure(Exception("Fid is null"));
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveFid(fid: String): Result<Unit> {
        return withContext(ioDispatcher) {
            preferenceDao.saveFid(fid)
            return@withContext Result.success(Unit)
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
                    token = token,
                    starrableId = githubRepoModel.serverId
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
                    token = token,
                    starrableId = githubRepoModel.serverId
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
