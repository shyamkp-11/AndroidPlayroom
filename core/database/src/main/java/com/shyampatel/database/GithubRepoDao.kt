package com.shyampatel.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Same table storing max star and all other repositories. Better implementation future.
 */
@Dao
interface GithubRepoDao {

    @Query(
        value = "SELECT * FROM github_repo ORDER BY stars DESC LIMIT 50"
    )
    fun getMaxStarGithubRepoEntities(): Flow<List<GithubRepoEntity>>

    @Query(
        value = "SELECT * FROM github_repo WHERE github_repo.ownerId = :ownerId"
    )
    fun getRepoEntitiesForOwner(ownerId: String): Flow<List<GithubRepoEntity>>

    @Query(
        value = """
        SELECT * FROM github_repo
        WHERE serverId IN (:serverIds)
    """,
    )
    fun getGithubRepoEntities(serverIds: List<String>): Flow<List<GithubRepoEntity>>

    @Query(
        value = """
            SELECT * FROM github_repo WHERE serverId IN (
        SELECT repoId FROM repo_starred WHERE userId = :userId
        )
    """,
    )
    fun getStarredGithubRepoEntitiesForUser(userId: String): Flow<List<GithubRepoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreGithubRepoEntities(githubRepoEntities: List<GithubRepoEntity>)

    @Upsert
    suspend fun upsertGithubRepoEntities(entities: List<GithubRepoEntity>)


    @Query(
        value = """
            DELETE FROM github_repo
            WHERE ownerId = :ownerId
        """,
    )
    suspend fun deleteGithubRepoEntitiesForOwner(ownerId: String)

    @Query(
        value = """
            DELETE FROM github_repo
            WHERE serverId in (:ids)
        """,
    )
    suspend fun deleteGithubRepoEntities(ids: List<String>)

    @Query(
        value = """
            DELETE FROM github_repo
        """
    )
    suspend fun deleteAllRepositories()
}