package com.shyampatel.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RepoOwnerDao {

    @Query(
        value = "SELECT * FROM repo_owner"
    )
    fun getRepoOwnerEntities(): Flow<List<RepoOwnerEntity>>

    @Query(
        value = """
        SELECT * FROM repo_owner
        WHERE serverId IN (:ids)
    """,
    )
    fun getRepoOwnerEntities(ids: Set<String>): Flow<List<RepoOwnerEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreRepoOwnerEntities(repoOwnerEntities: List<RepoOwnerEntity>)

    @Upsert
    suspend fun upsertRepoOwnerEntities(entities: List<RepoOwnerEntity>)

    @Query(
        value = """
            DELETE FROM repo_owner
            WHERE serverId in (:ids)
        """,
    )
    suspend fun deleteRepoOwnerEntities(ids: List<Int>)

    @Query(
        value = """
            DELETE FROM repo_owner
        """
    )
    suspend fun deleteRepoOwnerData()
}