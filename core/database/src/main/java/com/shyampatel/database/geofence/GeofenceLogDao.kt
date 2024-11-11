package com.shyampatel.database.geofence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceLogDao {

    @Query("SELECT * FROM geofence_log_entity WHERE id = :id")
    fun getGeofenceLogs(id: Long): Flow<List<GeofenceLogEntity>>

    @Query("DELETE FROM geofence_log_entity WHERE id = :id")
    suspend fun deleteGeofenceLogs(id: Long): Int

    @Query("DELETE FROM geofence_log_entity")
    suspend fun deleteAllGeofenceLogs()

    // TODO test abort strategy
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGeofenceLog(geofenceLogEntity: GeofenceLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGeofenceLogs(geofenceLogEntities: List<GeofenceLogEntity>): List<Long>
}