package com.shyampatel.database

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module

fun getDatabaseModule(applicationContext: Context) = module {
    single<GithubDatabase> {
        Room.databaseBuilder(
            applicationContext,
            GithubDatabase::class.java,
            "github-repo-database",
        ).build()
    }
    factory<GithubRepoDao> {
        get<GithubDatabase>().githubRepoDao()
    }
    factory<RepoOwnerDao>{
        get<GithubDatabase>().repoOwnerDao()
    }
    factory<StarredDao>{
        get<GithubDatabase>().starredDao()
    }
}