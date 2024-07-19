package com.shyampatel.core.data

import android.content.Context
import com.shyampatel.database.GithubRepoDao
import com.shyampatel.database.getDatabaseModule
import com.shyampatel.datastore.getDataStoreModule
import com.shyampatel.network.getNetworkModule
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

fun getDataModule(
    baseUrl: String,
    applicationContext: Context,
    defaultDispatcher: CoroutineDispatcher,
    ioDispatcher: CoroutineDispatcher
) = module {
    // This way not exposing the network module up the dependency tree.
    includes(
        getNetworkModule(applicationContext = applicationContext, baseUrl = baseUrl, ioDispatcher = ioDispatcher),
        getDatabaseModule(applicationContext = applicationContext),
        getDataStoreModule(applicationContext = applicationContext, ioDispatcher = ioDispatcher)
    )
    factory<GithubRepository> {
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