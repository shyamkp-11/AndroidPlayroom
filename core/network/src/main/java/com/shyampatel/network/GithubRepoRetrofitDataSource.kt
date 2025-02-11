package com.shyampatel.network

import com.shyampatel.core.network.BuildConfig
import kotlinx.serialization.json.JsonObject
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
            BuildConfig.CLIENT_ID_OAUTH_APP,
            BuildConfig.CLIENT_SECRET_OAUTH_APP
        ),
        @Path("client_id") clientId: String = BuildConfig.CLIENT_ID_OAUTH_APP,
        @Body requestBody: Map<String, String>
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "/applications/{client_id}/grant", hasBody = true)
    suspend fun deleteAppAuthorization(
        @Header("Authorization") credentials: String = Credentials.basic(
            BuildConfig.CLIENT_ID_OAUTH_APP,
            BuildConfig.CLIENT_SECRET_OAUTH_APP
        ),
        @Path("client_id") clientId: String = BuildConfig.CLIENT_ID_OAUTH_APP,
        @Body requestBody: Map<String, String>
    ): Response<Unit>

    @Headers("Accept: application/json")
    @POST
    suspend fun generateUserAccessToken(
        @Url url: String = "https://github.com/login/oauth/access_token",
        @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET_OAUTH_APP,
        @Query("code") code: String,
        @Query("client_id") clientId: String = BuildConfig.CLIENT_ID_OAUTH_APP,
    ): Response<NetworkAccessTokenModel>

    @Headers("X-Github-Next-Global-ID: 1")
    @GET("/users/{username}/installation")
    suspend fun getAppInstallationForUser(
        @Header("Authorization") jwtToken: String,
        @Path("username") username: String,
    ): Response<Unit>

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
        @Url url: String,
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Response<NetworkFcmEnabled>

    @PUT
    suspend fun appServerFcmEnabled(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body requestBody: JsonObject,
    ): Response<Unit>

    @POST
    suspend fun appServerNotifySignOut(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String>
    ): Response<Unit>

    @PUT
    suspend fun appServerUpdateFcmToken(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body requestBody: JsonObject,
    ): Response<Unit>
}

/**
 * Find a better way to inject different base urls.
 */
internal class GithubRepoRetrofitDataSourceHelper(private val networkApi: RetrofitGithubRepoNetworkApi, private val appServerBaseUrl: String): RetrofitGithubRepoNetworkApi {
    override suspend fun getMaxStartsGithubRepo(): Response<NetworkGithubRepoWrapper> {
        return networkApi.getMaxStartsGithubRepo()
    }

    override suspend fun getGithubRepo(
        token: String?,
        owner: String,
        repo: String
    ): Response<NetworkGithubRepoModel> {
        return networkApi.getGithubRepo(token, owner, repo)
    }

    override suspend fun deleteAppToken(
        credentials: String,
        clientId: String,
        requestBody: Map<String, String>
    ): Response<Unit> {
        return networkApi.deleteAppToken(credentials, clientId, requestBody)
    }

    override suspend fun deleteAppAuthorization(
        credentials: String,
        clientId: String,
        requestBody: Map<String, String>
    ): Response<Unit> {
        return networkApi.deleteAppAuthorization(credentials, clientId, requestBody)
    }

    override suspend fun generateUserAccessToken(
        url: String,
        clientSecret: String,
        code: String,
        clientId: String
    ): Response<NetworkAccessTokenModel> {
        return networkApi.generateUserAccessToken(url, clientSecret, code, clientId)
    }

    override suspend fun getAppInstallationForUser(
        jwtToken: String,
        username: String
    ): Response<Unit> {
        return networkApi.getAppInstallationForUser(jwtToken, username)
    }

    override suspend fun getAuthenticatedOwner(token: String): Response<NetworkRepoOwner> {
        return networkApi.getAuthenticatedOwner(token)
    }

    override suspend fun getMyRepositories(token: String): Response<List<NetworkGithubRepoModel>> {
        return networkApi.getMyRepositories(token)
    }

    override suspend fun searchRepositories(
        queryString: String,
        sort: String,
        order: String,
        perPage: Int
    ): Response<NetworkGithubRepoWrapper> {
        return networkApi.searchRepositories(queryString, sort, order, perPage)
    }

    override suspend fun starRepository(
        token: String,
        owner: String,
        repo: String
    ): Response<Unit> {
        return networkApi.starRepository(token, owner, repo)
    }

    override suspend fun unstarRepository(
        token: String,
        owner: String,
        repo: String
    ): Response<Unit> {
        return networkApi.unstarRepository(token, owner, repo)
    }

    override suspend fun getStarredRepositories(token: String): Response<List<NetworkGithubRepoModel>> {
        return networkApi.getStarredRepositories(token)
    }

    override suspend fun appServerSignedInToApp(
        url: String,
        token: String,
        requestBody: Map<String, String>
    ): Response<NetworkFcmEnabled> {
        val newUrl = "$appServerBaseUrl/api/v1/githubUsers/signedInToApp"
        return networkApi.appServerSignedInToApp(newUrl, token, requestBody)
    }

    override suspend fun appServerFcmEnabled(
        url: String,
        token: String,
        requestBody: JsonObject
    ): Response<Unit> {
        val newUrl = "$appServerBaseUrl/api/v1/githubUsers/fcmEnabled"
        return networkApi.appServerFcmEnabled(newUrl, token, requestBody)
    }

    override suspend fun appServerNotifySignOut(
        url: String,
        token: String,
        requestBody: Map<String, String>
    ): Response<Unit> {
        val newUrl = "$appServerBaseUrl/api/v1/githubUsers/signout"
        return networkApi.appServerNotifySignOut(newUrl, token, requestBody)
    }

    override suspend fun appServerUpdateFcmToken(
        url: String,
        token: String,
        requestBody: JsonObject
    ): Response<Unit> {
        val newUrl = "$appServerBaseUrl/api/v1/githubUsers/fcmToken"
        return networkApi.appServerUpdateFcmToken(newUrl, token, requestBody)
    }
}