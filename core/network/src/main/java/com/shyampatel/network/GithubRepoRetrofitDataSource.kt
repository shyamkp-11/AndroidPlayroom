package com.shyampatel.network

import com.shyampatel.core.network.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.Credentials
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.net.URLEncoder

internal interface RetrofitGithubRepoNetworkApi {


    @Headers("Accept: application/json",
        "X-Github-Next-Global-ID: 1")
    @GET(value = "search/repositories?q=stars:>5&sort=stars&order=desc&per_page=50")
    suspend fun getMaxStartsGithubRepo(): Response<NetworkGithubRepoWrapper>

    @Headers("X-Github-Next-Global-ID: 1")
    @GET(value = "/repos/{owner}/{repo}")
    suspend fun getGithubRepo(
        @Header("Authorization") token: String? = null,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<NetworkGithubRepoModel>

    @HTTP(method = "DELETE", path = "/applications/{client_id}/token", hasBody = true)
    suspend fun deleteAppToken(
        @Header("Authorization") credentials: String = Credentials.basic(
            BuildConfig.CLIENT_ID,
            BuildConfig.CLIENT_SECRET
        ),
        @Path("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Body requestBody: Map<String, String>
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "/applications/{client_id}/grant", hasBody = true)
    suspend fun deleteAppAuthorization(
        @Header("Authorization") credentials: String = Credentials.basic(
            BuildConfig.CLIENT_ID,
            BuildConfig.CLIENT_SECRET
        ),
        @Path("client_id") clientId: String = BuildConfig.CLIENT_ID,
        @Body requestBody: Map<String, String>
    ): Response<Unit>

    @Headers("Accept: application/json")
    @POST
    suspend fun generateUserAccessToken(
        @Url url: String = "https://github.com/login/oauth/access_token",
        @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET,
        @Query("code") code: String,
        @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
    ): Response<NetworkAccessTokenModel>

    @Headers("X-Github-Next-Global-ID: 1")
    @GET(value = "/user")
    suspend fun getAuthenticatedOwner(@Header("Authorization") token: String): Response<NetworkRepoOwner>

    @Headers("X-Github-Next-Global-ID: 1")
    @GET(value = "/user/repos")
    suspend fun getMyRepositories(@Header("Authorization") token: String): Response<List<NetworkGithubRepoModel>>

    @Headers("X-Github-Next-Global-ID: 1")
    @GET(value = "search/repositories")
    suspend fun searchRepositories(
        @Query("q") queryString: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 50,
    ): Response<NetworkGithubRepoWrapper>

    @Headers("X-Github-Next-Global-ID: 1")
    @PUT(value = "/user/starred/{owner}/{repo}")
    suspend fun starRepository(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    @DELETE(value = "/user/starred/{owner}/{repo}")
    suspend fun unstarRepository(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    @Headers("X-Github-Next-Global-ID: 1")
    @GET(value = "/user/starred")
    suspend fun getStarredRepositories(@Header("Authorization") token: String): Response<List<NetworkGithubRepoModel>>

    @POST
    suspend fun appServerSignedInToApp(
        @Url url: String = "${BuildConfig.APP_SERVER_BASE_URL}/api/v1/githubUsers/signedInToApp",
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Response<NetworkFcmEnabled>

    @PUT
    suspend fun appServerFcmEnabled(
        @Url url: String = "${BuildConfig.APP_SERVER_BASE_URL}/api/v1/githubUsers/fcmEnabled",
        @Header("Authorization") token: String,
        @Body requestBody: JsonObject,
    ): Response<Unit>

    @POST
    suspend fun appServerNotifySignOut(
        @Url url: String = "${BuildConfig.APP_SERVER_BASE_URL}/api/v1/githubUsers/signout",
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Response<Unit>

    @PUT
    suspend fun appServerUpdateFcmToken(
        @Url url: String = "${BuildConfig.APP_SERVER_BASE_URL}/api/v1/githubUsers/fcmToken",
        @Header("Authorization") token: String,
        @Body requestBody: JsonObject,
    ): Response<Unit>

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

    override fun getGithubRepo(
        token: String?,
        owner: String,
        repo: String
    ): Flow<NetworkGithubRepoModel> {
        return flow {
            emit(
                networkApi.getGithubRepo(
                    token = token,
                    owner = owner,
                    repo = repo
                ).body()!!
            )
        }.flowOn(ioDispatcher)
    }

    override suspend fun appServerNotifySignOut(
        globalId: String,
        deviceId: String,
        fcmToken: String
    ): Boolean {
        return withContext(ioDispatcher) {
            return@withContext networkApi.appServerNotifySignOut(
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
                requestBody = buildJsonObject {
                    put("globalId", globalId)
                    put("deviceId", deviceId)
                    put("fcmToken", fcmToken)
                },
                token = "Bearer ${BuildConfig.APP_SERVER_TOKEN}",
            ).isSuccessful
        }
    }

    override suspend fun appServerSaveNotificationEnabled(globalId: String, deviceId: String, fcmEnabled: Boolean, fcmToken: String): Boolean {
        return withContext(ioDispatcher) {
            return@withContext networkApi.appServerFcmEnabled(
                requestBody =buildJsonObject {
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
            return@withContext response.body()?.fcmEnabled ?: throw Exception("appServerSignedInToApp response is $response")
        }
    }

    override suspend fun deleteUserAccessToken(token: String): Boolean {
        return withContext(ioDispatcher) {
            val response =
                networkApi.deleteAppToken(requestBody = mapOf(Pair("access_token", token)))
            response.code() == 204
        }
    }

    override suspend fun getAuthenticatedOwner(token: String): NetworkRepoOwner {
        return withContext(ioDispatcher) {
            val response = networkApi.getAuthenticatedOwner(token = "Bearer $token")
            if (response.isSuccessful) {
                response.body()!!
            } else throw Exception(
                "Api response code: ${response.code()}. Error body:${
                    response.errorBody()?.string()
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
            val response =
                networkApi.starRepository(owner = owner, repo = repo, token = "Bearer $token")
            response.code() == 204
        }
    }

    override suspend fun unstarRepository(token: String, owner: String, repo: String): Boolean {
        return withContext(ioDispatcher) {
            val response =
                networkApi.unstarRepository(owner = owner, repo = repo, token = "Bearer $token")
            response.code() == 204
        }
    }
}