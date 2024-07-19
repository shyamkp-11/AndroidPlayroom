package com.shyampatel.network

import com.shyampatel.core.network.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.net.URLEncoder

internal interface RetrofitGithubRepoNetworkApi {
    @GET(value = "search/repositories?q=stars:>5&sort=stars&order=desc&per_page=50")
    suspend fun getMaxStartsGithubRepo(): Response<NetworkGithubRepoWrapper>

    @GET(value = "/repos/{owner}/{repo}")
    suspend fun getGithubRepo(@Header("Authorization") token: String? = null, @Path("owner") owner: String, @Path("repo") repo: String): Response<NetworkGithubRepoModel>

    @HTTP(method = "DELETE", path = "/applications/{client_id}/grant", hasBody = true)
    suspend fun deleteAppAuthorization(@Header("Authorization") credentials: String = Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET),
                                       @Path("client_id") clientId: String = BuildConfig.CLIENT_ID,
                                       @Body requestBody: Map<String, String>): Response<Unit>

    @Headers("Accept: application/json")
    @POST
    suspend fun generateUserAccessToken(
        @Url url: String = "https://github.com/login/oauth/access_token",
        @Query("code") code: String,
        @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET
    ): Response<NetworkAccessTokenModel>

    @GET(value = "/user")
    suspend fun getAuthenticatedOwner(@Header("Authorization") token: String): Response<NetworkRepoOwner>
    @GET(value = "/user/repos")
    suspend fun getMyRepositories(@Header("Authorization") token: String): Response<List<NetworkGithubRepoModel>>
    @GET(value = "search/repositories")
    suspend fun searchRepositories(
        @Query("q") queryString: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 50,
    ): Response<NetworkGithubRepoWrapper>

    @PUT(value = "/user/starred/{owner}/{repo}")
    suspend fun starRepository(@Header("Authorization") token: String, @Path("owner") owner: String, @Path("repo") repo: String): Response<Unit>

    @DELETE(value = "/user/starred/{owner}/{repo}")
    suspend fun unstarRepository(@Header("Authorization") token: String, @Path("owner") owner: String, @Path("repo") repo: String): Response<Unit>

    @GET(value = "/user/starred")
    suspend fun getStarredRepositories(@Header("Authorization") token: String): Response<List<NetworkGithubRepoModel>>
}

internal class GithubRepoRetrofitDataSource(
    private val networkApi: RetrofitGithubRepoNetworkApi,
    private val ioDispatcher: CoroutineDispatcher
) : GithubRepoRemoteDataSource {

    override fun getMaxStarsGithubRepo(): Flow<List<NetworkGithubRepoModel>> {
        return flow {
            emit(networkApi.getMaxStartsGithubRepo().body()!!.items)
        }.flowOn(ioDispatcher)
    }

    override fun getGithubRepo(token: String?, owner: String ,repo: String): Flow<NetworkGithubRepoModel> {
        return flow {
            emit(networkApi.getGithubRepo(
                token = token,
                owner = owner,
                repo = repo).body()!!)
        }.flowOn(ioDispatcher)
    }

    override suspend fun deleteUserAccessToken(token: String): Boolean {
        return withContext(ioDispatcher) {
            val response = networkApi.deleteAppAuthorization(requestBody = mapOf(Pair("access_token",token)))
            response.code() == 204
        }
    }

    override suspend fun getAuthenticatedOwner(token: String): NetworkRepoOwner {
        return  withContext(ioDispatcher) {
            val response = networkApi.getAuthenticatedOwner(token = "Bearer $token")
            if (response.isSuccessful) {
                response.body()!!
            } else throw Exception("Api response code: ${response.code()}. Error body:${response.errorBody()}")
        }
    }

    override suspend fun generateUserAccessToken(code: String): String {
        return withContext(ioDispatcher) {
            val result = networkApi.generateUserAccessToken(code = code)
            if (result.isSuccessful) result.body()!!.accessToken else ""
        }
    }

    override fun getMyRepositories(token: String): Flow<List<NetworkGithubRepoModel>> {
        return flow {
            emit(networkApi.getMyRepositories(token = "Bearer $token").body()!!)
        }.flowOn(ioDispatcher)
    }

    override fun getMyStarredRepositories(token: String): Flow<List<NetworkGithubRepoModel>> {
        return flow {
            emit(networkApi.getStarredRepositories(token = "Bearer $token").body()!!)
        }.flowOn(ioDispatcher)
    }

    override fun searchRepositories(searchQuery: String): Flow<List<NetworkGithubRepoModel>> {
        val encodedQuery = URLEncoder.encode(searchQuery, Charsets.UTF_8.name())
        return flow {
            emit(networkApi.searchRepositories(encodedQuery).body()!!.items)
        }.flowOn(ioDispatcher)
    }

    override suspend fun starRepository(token: String, owner: String, repo: String): Boolean {
        return withContext(ioDispatcher) {
            val response = networkApi.starRepository(owner = owner, repo= repo, token = "Bearer $token")
            response.code() == 204
        }
    }

    override suspend fun unstarRepository(token: String, owner: String, repo: String): Boolean {
        return withContext(ioDispatcher) {
            val response = networkApi.unstarRepository(owner = owner, repo= repo, token = "Bearer $token")
            response.code() == 204
        }
    }
}