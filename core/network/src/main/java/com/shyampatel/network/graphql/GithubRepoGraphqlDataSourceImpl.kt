package com.shyampatel.network.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.http.HttpHeader
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.core.network.BuildConfig
import com.shyampatel.network.RetrofitGithubRepoNetworkApi
import com.shyampatel.network.graphql.type.AddStarInput
import com.shyampatel.network.graphql.type.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.internal.http2.Header
import java.net.URLEncoder

internal class GithubRepoGraphqlDataSourceImpl(
    private val networkApi: RetrofitGithubRepoNetworkApi,
    private val apolloClient: ApolloClient,
    private val ioDispatcher: CoroutineDispatcher
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
                networkApi.deleteAppAuthorization(requestBody = mapOf(Pair("access_token", token)))
            response.code() == 204
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
            val clientId = BuildConfig.CLIENT_ID
            val clientSecret = BuildConfig.CLIENT_SECRET
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

    override fun getOwnerData(token: String, userLogin: String, ownerType: RepoOwnerType): Flow<List<GithubRepoModel>> {
        return flow {
            val header = HttpHeader("Authorization", "Bearer $token")

            if (ownerType == RepoOwnerType.ORGANIZATION) {
                val response = apolloClient.query(RepoOwnerOrganizationWithRepoQuery(userLogin)).run {
                    httpHeaders(listOf(header))
                    execute().data!!.organization!!.run {
                        organization.repositories.edges!!.map { it!!.node!!.repository.asGithubRepoEntity() }
                    }
                }
                emit(response)
            } else {
                val response = apolloClient.query(RepoOwnerUserWithRepoAndStarsQuery(userLogin)).run {
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

    override fun getStarRepositories(token: String, userLogin: String): Flow<List<GithubRepoModel>> {
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

    override fun searchRepositories(token: String?, searchQuery: String): Flow<List<GithubRepoModel>> {
        val encodedQuery = URLEncoder.encode(searchQuery, Charsets.UTF_8.name())
        return flow {
            val header = if (token != null) HttpHeader("Authorization", "Bearer $token") else null
            val response = apolloClient.query(SearchRepositoriesQuery(encodedQuery, Optional.present(25), Optional.absent())).run {
                if (header != null) { httpHeaders(listOf(header)) }
                execute().data!!.search.edges!!.map { it!!.node!!.repository!!.asGithubRepoEntity() }
            }
            emit(response)
        }.flowOn(ioDispatcher)
    }

    override suspend fun starRepository(token: String, starrableId: String): Boolean {
        return withContext(ioDispatcher) {
            val header = HttpHeader("Authorization", "Bearer $token")
            val response =
                apolloClient.mutation(AddStarMutation(AddStarInput(starrableId = starrableId))).run {
                    httpHeaders(listOf(header))
                    execute().data!!.addStar!!.starrable!!.starrable.viewerHasStarred
                }
            return@withContext response
        }
    }

    override suspend fun unstarRepository(token: String, starrableId: String): Boolean {
        return withContext(ioDispatcher) {
            val header = HttpHeader("Authorization", "Bearer $token")
            val response = apolloClient.mutation(RemoveStarMutation(starrableId = starrableId)).run {
                httpHeaders(listOf(header))
                !execute().data!!.removeStar!!.starrable!!.starrable.viewerHasStarred
            }
            return@withContext response
        }
    }
}