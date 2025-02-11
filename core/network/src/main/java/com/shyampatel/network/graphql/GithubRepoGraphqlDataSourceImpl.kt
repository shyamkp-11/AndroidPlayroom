package com.shyampatel.network.graphql

import android.net.Uri
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.http.HttpHeader
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.core.network.BuildConfig
import com.shyampatel.network.GithubRepoRetrofitDataSourceHelper
import com.shyampatel.network.graphql.type.AddStarInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class GithubRepoGraphqlDataSourceImpl(
    private val networkApi: GithubRepoRetrofitDataSourceHelper,
    private val apolloClient: ApolloClient,
    private val ioDispatcher: CoroutineDispatcher,
) : GithubRepoGraphqlDataSource {

    override fun getGithubRepo(
        token: String?,
        owner: String,
        repo: String
    ): Flow<GithubRepoModel> {
        return flow {
            val header = if (token != null) HttpHeader("Authorization", "Bearer $token") else null
            emit(
                apolloClient.query(GetRepositoryQuery(owner = owner, repoName = repo)).run {
                    if (header != null) httpHeaders(listOf(header))
                    execute()
                }
                    .dataOrThrow().repository?.repository?.asGithubRepoEntity() ?: throw Exception()
            )
        }.flowOn(ioDispatcher)
    }

    override suspend fun deleteUserAccessToken(token: String): Boolean {
        return withContext(ioDispatcher) {
            val response =
                networkApi.deleteAppToken(requestBody = mapOf(Pair("access_token", token)))
            response.code() == 204
        }
    }

    override suspend fun getAppInstallationForUser(token: String, userLogin: String): Result<Unit> {
        return withContext(ioDispatcher) {
            val response = networkApi.getAppInstallationForUser(
                jwtToken = "Bearer $token",
                username = userLogin
            )
            return@withContext if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("User has not installed the app"))
        }
    }

    override suspend fun getAuthenticatedOwner(token: String): GetAuthenticatedRepoOwnerQuery.Viewer {
        return withContext(ioDispatcher) {
            val header = HttpHeader("Authorization", "Bearer $token")
            val response = apolloClient.query(GetAuthenticatedRepoOwnerQuery()).run {
                httpHeaders(listOf(header))
                execute()
            }
            return@withContext response.data?.viewer ?: throw Exception(
                "Errors are ${
                    response.errors?.toString()
                }"
            )
        }
    }

    override suspend fun generateUserAccessToken(code: String): String {
        return withContext(ioDispatcher) {
            val clientId = BuildConfig.CLIENT_ID_OAUTH_APP
            val clientSecret = BuildConfig.CLIENT_SECRET_OAUTH_APP
            val result = networkApi.generateUserAccessToken(
                clientId = clientId,
                code = code,
                clientSecret = clientSecret
            )
            if (result.isSuccessful) result.body()!!.accessToken else throw Exception(
                "Response code " + result.code() + ". Error:" + result.errorBody()?.string()
            )
        }
    }

    override fun getOwnerData(
        token: String,
        userLogin: String,
        ownerType: RepoOwnerType
    ): Flow<List<GithubRepoModel>> {
        return flow {
            val header = HttpHeader("Authorization", "Bearer $token")

            if (ownerType == RepoOwnerType.ORGANIZATION) {
                val response =
                    apolloClient.query(RepoOwnerOrganizationWithRepoQuery(userLogin)).run {
                        httpHeaders(listOf(header))
                        execute().data!!.organization!!.run {
                            organization.repositories.edges!!.map { it!!.node!!.repository.asGithubRepoEntity() }
                        }
                    }
                emit(response)
            } else {
                val response =
                    apolloClient.query(RepoOwnerUserWithRepoAndStarsQuery(userLogin)).run {
                        httpHeaders(listOf(header))
                        execute().data!!.user!!.run {
                            user.repositories.edges!!.map { it!!.node!!.repository.asGithubRepoEntity() }
                            /*.plus(user.starredRepositories.edges!!.map { it!!.node.repository.asGithubRepoEntity() })*/
                        }
                    }
                emit(response)
            }
        }.flowOn(ioDispatcher)
    }

    override fun getStarRepositories(
        token: String,
        userLogin: String
    ): Flow<List<GithubRepoModel>> {
        return flow {
            val header = HttpHeader("Authorization", "Bearer $token")
            val response = apolloClient.query(RepoOwnerUserWithRepoAndStarsQuery(userLogin)).run {
                httpHeaders(listOf(header))
                execute().data!!.user!!.run {
                    user.starredRepositories.edges!!.map { it!!.node.repository.asGithubRepoEntity() }
                }
            }
            emit(response)
        }.flowOn(ioDispatcher)
    }

    override suspend fun appServerNotifySignOut(
        globalId: String,
        deviceId: String,
        fcmToken: String
    ): Boolean {
        return withContext(ioDispatcher) {
            return@withContext networkApi.appServerNotifySignOut(
                url = "",
                requestBody = mapOf(
                    Pair("globalId", globalId),
                    Pair("deviceId", deviceId),
                    Pair("fcmToken", fcmToken)
                ),
                token = "Bearer ${BuildConfig.APP_SERVER_TOKEN}",
            ).isSuccessful
        }
    }

    override suspend fun appServerSaveFcmToken(
        globalId: String,
        deviceId: String,
        fcmToken: String
    ): Boolean {
        return withContext(ioDispatcher) {
            return@withContext networkApi.appServerUpdateFcmToken(
                url = "",
                requestBody = buildJsonObject {
                    put("globalId", globalId)
                    put("deviceId", deviceId)
                    put("fcmToken", fcmToken)
                },
                token = "Bearer ${BuildConfig.APP_SERVER_TOKEN}",
            ).isSuccessful
        }
    }

    override suspend fun appServerSaveNotificationEnabled(
        globalId: String,
        deviceId: String,
        fcmEnabled: Boolean,
        fcmToken: String
    ): Boolean {
        return withContext(ioDispatcher) {
            return@withContext networkApi.appServerFcmEnabled(
                url = "",
                requestBody = buildJsonObject {
                    put("globalId", globalId)
                    put("deviceId", deviceId)
                    put("fcmEnabled", fcmEnabled)
                    put("fcmToken", fcmToken)
                },
                token = "Bearer ${BuildConfig.APP_SERVER_TOKEN}",
            ).isSuccessful
        }
    }

    override suspend fun appServerSignedInToApp(
        fcmToken: String,
        deviceId: String,
        globalId: String,
        userLogin: String,
        firstName: String,
        lastName: String,
        email: String
    ): Boolean {
        return withContext(ioDispatcher) {
            val response = networkApi.appServerSignedInToApp(
                url = "",
                requestBody = mapOf(
                    Pair("fcmToken", fcmToken),
                    Pair("deviceId", deviceId),
                    Pair("globalId", globalId),
                    Pair("username", userLogin),
                    Pair("firstName", firstName),
                    Pair("lastName", lastName),
                ),
                token = "Bearer ${BuildConfig.APP_SERVER_TOKEN}",
            )
            return@withContext response.body()?.fcmEnabled
                ?: throw Exception("appServerSignedInToApp response is $response")
        }
    }

    override fun searchRepositories(
        token: String?,
        searchQuery: String
    ): Flow<List<GithubRepoModel>> {
        val encodedQuery = Uri.encode(searchQuery)
        return flow {
            val header = if (token != null) HttpHeader("Authorization", "Bearer $token") else null
            val response = apolloClient.query(
                SearchRepositoriesQuery(
                    encodedQuery,
                    Optional.present(25),
                    Optional.absent()
                )
            ).run {
                if (header != null) {
                    httpHeaders(listOf(header))
                }
                execute().data!!.search.edges!!.map { it!!.node!!.repository!!.asGithubRepoEntity() }
            }
            emit(response)
        }.flowOn(ioDispatcher)
    }

    override suspend fun starRepository(token: String, starrableId: String): Boolean {
        return withContext(ioDispatcher) {
            val header = HttpHeader("Authorization", "Bearer $token")
            val response =
                apolloClient.mutation(AddStarMutation(AddStarInput(starrableId = starrableId)))
                    .run {
                        httpHeaders(listOf(header))
                        execute().data!!.addStar!!.starrable!!.starrable.viewerHasStarred
                    }
            return@withContext response
        }
    }

    override suspend fun unstarRepository(token: String, starrableId: String): Boolean {
        return withContext(ioDispatcher) {
            val header = HttpHeader("Authorization", "Bearer $token")
            val response =
                apolloClient.mutation(RemoveStarMutation(starrableId = starrableId)).run {
                    httpHeaders(listOf(header))
                    !execute().data!!.removeStar!!.starrable!!.starrable.viewerHasStarred
                }
            return@withContext response
        }
    }
}