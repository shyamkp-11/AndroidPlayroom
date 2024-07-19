package com.shyampatel.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shyampatel.database.util.RepoOwnerTypeConverter

@Database(
    entities = [GithubRepoEntity::class, RepoOwnerEntity::class, StarredEntity::class],
    version = 4
)
@TypeConverters(
    RepoOwnerTypeConverter::class,
)
internal abstract class GithubDatabase: RoomDatabase() {
    abstract fun githubRepoDao(): GithubRepoDao
    abstract fun repoOwnerDao(): RepoOwnerDao
    abstract fun starredDao(): StarredDao
}