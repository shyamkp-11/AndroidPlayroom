package com.shyampatel.database.geofence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface GeofenceRepoDao {

    @Query(
        value = "SELECT * FROM geofence_entity"
    )
    fun getGeofenceEntities(): Flow<List<GeofenceEntity>>

    @Query(
        value = "SELECT * FROM geofence_entity WHERE id = :id"
    )
    fun getGeofenceEntity(id: Long): Flow<GeofenceEntity>

    @Query(
        value = "SELECT * FROM geofence_entity WHERE id IN (:ids)"
    )
    fun getGeofenceEntities(ids: List<Long>): Flow<List<GeofenceEntity>>

    @Query(
        value = "SELECT * FROM geofence_entity WHERE rowid = :rowId"
    )
    fun getGeofenceEntityForRowId(rowId: Long): Flow<GeofenceEntity>

    /**
     * Deletes rows in the db matching the specified [id]s. Returns the number of rows deleted.
     */
    @Query(
        value = "DELETE FROM geofence_entity WHERE id = :id"
    )
    suspend fun deleteGeofenceEntity(id: Long): Int

    @Query(
        value = "DELETE FROM geofence_entity"
    )
    suspend fun deleteAllGeofenceEntities()

    @Query(
        value = "SELECT * FROM geofence_entity WHERE createdAt = :createdAt"
    )
    suspend fun getGeofenceEntities(createdAt: Date): List<GeofenceEntity>

    /**
     * Inserts [githubRepoEntities] into the db if they don't exist, and ignores those that do. Returns -1 in case of conflict else returns the rowId.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreGeofenceEntity(githubRepoEntities: GeofenceEntity): Long

    suspend fun insertOrIgnoreAndUpdateTimestamps(githubRepoEntities: GeofenceEntity): Long {
        return insertOrIgnoreGeofenceEntity(githubRepoEntities.copy(createdAt = Date(System.currentTimeMillis()), modifiedAt = Date(System.currentTimeMillis())))
    }

    /**
     * Inserts [githubRepoEntities] into the db if they don't exist, and ignores those that do. Returns -1 in case of conflict else returns the rowId.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreGeofenceEntities(githubRepoEntities: List<GeofenceEntity>): List<Long>

    suspend fun insertOrIgnoreAndUpdateTimestamps(githubRepoEntities: List<GeofenceEntity>): List<Long> {
        return githubRepoEntities.map {
            it.copy(createdAt = Date(System.currentTimeMillis()), modifiedAt = Date(System.currentTimeMillis()))
        }.let {
            insertOrIgnoreGeofenceEntities(it)
        }
    }

    /**
     * Updates [entities] in the db that match the primary key, and insert if they don't. Returns -1 in case of conflict else returns the rowId.
     */
    @Upsert
    suspend fun upsertGeofenceEntities(entities: List<GeofenceEntity>): List<Long>

    suspend fun upsertWithTimeStampsUpdate(entities: List<GeofenceEntity>): List<Long> {
        return entities.map {
            it.copy(modifiedAt = Date(System.currentTimeMillis()))
        }.let {
            upsertGeofenceEntities(it)
        }
    }
}