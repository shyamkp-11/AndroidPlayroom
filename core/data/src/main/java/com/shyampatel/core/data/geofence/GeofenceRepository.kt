package com.shyampatel.core.data.geofence

import android.graphics.Bitmap
import com.shyampatel.core.common.geofence.Geofence
import com.shyampatel.core.common.geofence.GeofenceLog
import com.shyampatel.core.common.geofence.LatLong
import kotlinx.coroutines.flow.Flow

interface GeofenceRepository {
    fun getGeofences(): Flow<Result<List<Geofence>>>
    fun getGeofence(id: Long): Flow<Result<Geofence>>
    fun getGeofences(ids: List<Long>): Flow<Result<List<Geofence>>>
    fun getGeofenceForRowId(rowId: Long): Flow<Result<Geofence>>
    suspend fun addGeofence(latLong: LatLong, radius: Double, name: String, bitmap: Bitmap? = null): Result<Long>
    suspend fun removeGeofence(id: Long): Result<Unit>
    suspend fun upsertGeofences(geofences: List<Geofence>): Result<List<Long>>
    suspend fun addGeofenceLog(geofenceLog: GeofenceLog): Result<Long>
    suspend fun addGeofenceLog(geofenceLogs: List<GeofenceLog>): Result<List<Long>>
    fun getGeofenceLogs(geofenceId: Long): Flow<Result<List<GeofenceLog>>>
    suspend fun removeGeofenceLogs(id: Long): Result<Unit>
    suspend fun saveLastLocation(it: LatLong)
    fun getLastLocation(): Flow<Result<LatLong>>
}