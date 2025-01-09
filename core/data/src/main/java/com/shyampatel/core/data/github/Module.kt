package com.shyampatel.core.data.github

import android.content.Context
import com.shyampatel.core.data.BuildConfig
import com.shyampatel.core.data.github.graphql.GithubRepositoryImpl
import com.shyampatel.database.GithubRepoDao
import com.shyampatel.database.getDatabaseModule
import com.shyampatel.datastore.getDataStoreModule
import com.shyampatel.network.getNetworkModule
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

fun getDataModule(
    baseUrl: String,
    graphqlBaseUrl: String,
    applicationContext: Context,
    defaultDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher
) = module {
    // This way not exposing the network module up the dependency tree.
    includes(
        getNetworkModule(applicationContext = applicationContext, baseUrl = baseUrl, graphqlBaseUrl = graphqlBaseUrl, ioDispatcher = ioDispatcher),
        getDatabaseModule(applicationContext = applicationContext),
        getDataStoreModule(applicationContext = applicationContext, ioDispatcher = ioDispatcher)
    )

    factory<GithubRepository> {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.githubplayroom_api_mode == "REST") {
            com.shyampatel.core.data.github.GithubRepositoryImpl(
                githubRepoDataSource = get<GithubRepoDao>(),
                remoteDataSource = get(),
                defaultDispatcher = defaultDispatcher,
                preferenceDao = get(),
                repoOwnerDataSource = get(),
                starredDataSource = get(),
                ioDispatcher = ioDispatcher,
            )
        } else {
            GithubRepositoryImpl(
                githubRepoDataSource = get<GithubRepoDao>(),
                remoteDataSource = get(),
                defaultDispatcher = defaultDispatcher,
                preferenceDao = get(),
                repoOwnerDataSource = get(),
                starredDataSource = get(),
                ioDispatcher = ioDispatcher,
            )
        }
    }
}