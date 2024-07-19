package com.shyampatel.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StarredDao {
    @Query(
        value = """
            SELECT * FROM repo_starred
            WHERE userId in (:userId)
            """
    )
    fun getStarredEntitiesForUsers(userId: List<Long>): Flow<List<StarredEntity>>

    @Query(
        value = """
        SELECT * FROM repo_starred
        WHERE repoId = :repoId
        """,
    )
    fun getStarredEntitiesForRepo(repoId: Long): Flow<List<StarredEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreStarredEntities(starredEntities: List<StarredEntity>)

    @Upsert
    suspend fun upsertStarredEntities(starredEntities: List<StarredEntity>)

    @Delete
    suspend fun deleteStarredEntities(list: List<StarredEntity>)

    @Query(
        value = """
            DELETE FROM repo_starred WHERE userId = :userId 
        """
    )
    suspend fun deleteStarredForUser(userId: Long)

    @Query(
        value = """
            DELETE FROM repo_starred WHERE repoId = :repoId 
        """
    )
    suspend fun deleteStarredForRepo(repoId: Long)

    @Query(
        value = """
            DELETE FROM repo_starred
        """
    )
    suspend fun deleteAllStarredData()
}