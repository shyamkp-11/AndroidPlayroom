package com.shyampatel.datastore.geofence

import java.io.File

interface GeofenceFileDao {
    suspend fun <T> saveGeofenceImage(image: T, id: Long): Result<String>
    suspend fun <T> getGeofenceImage(id: Long): Result<File>
    suspend fun deleteGeofenceImage(id: Long): Result<Unit>
}