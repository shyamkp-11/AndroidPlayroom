package com.shyampatel.core.data.geofence

import android.graphics.Bitmap
import android.util.Log
import com.shyampatel.core.common.geofence.Geofence
import com.shyampatel.core.common.geofence.GeofenceLog
import com.shyampatel.core.common.geofence.LatLong
import com.shyampatel.database.geofence.GeofenceEntity
import com.shyampatel.database.geofence.GeofenceLogDao
import com.shyampatel.database.geofence.GeofenceRepoDao
import com.shyampatel.datastore.geofence.GeofenceDatastore
import com.shyampatel.datastore.geofence.GeofenceFileDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date


internal class GeofenceRepositoryImpl(
    val ioDispatcher: CoroutineDispatcher,
    val geofenceDatastore: GeofenceDatastore,
    val geofenceDao: GeofenceRepoDao,
    val geofenceFileDao: GeofenceFileDao,
    val geofenceLogDao: GeofenceLogDao,
) : GeofenceRepository {
    override fun getGeofences(): Flow<Result<List<Geofence>>> {
        return geofenceDao.getGeofenceEntities().map {
            Result.success(it.map { entity -> entity.toGeofence() })
        }.flowOn(ioDispatcher)
    }

    override fun getGeofences(ids: List<Long>): Flow<Result<List<Geofence>>> {
        return geofenceDao.getGeofenceEntities(ids).map {
            Result.success(it.map { entity -> entity.toGeofence() })
        }.flowOn(ioDispatcher)
    }
    override fun getGeofence(id: Long): Flow<Result<Geofence>> {
        return geofenceDao.getGeofenceEntity(id = id).map {
            Result.success(it.toGeofence())
        }.flowOn(ioDispatcher)
    }

    override fun getGeofenceForRowId(rowId: Long): Flow<Result<Geofence>> {
        return geofenceDao.getGeofenceEntityForRowId(rowId = rowId).map {
            Result.success(it.toGeofence())
        }.flowOn(ioDispatcher)
    }

    override suspend fun removeGeofence(id: Long): Result<Unit> {
        return withContext(ioDispatcher) {
            val deleted = geofenceDao.deleteGeofenceEntity(id)
            if (deleted == 1) {
                removeGeofenceLogs(id)

                return@withContext Result.success(Unit)
            } else{
                return@withContext Result.failure(Exception("Return value is not 1 but, $deleted"))
            }
        }.also { geofenceFileDao.deleteGeofenceImage(id = id) }
    }

    override suspend fun upsertGeofences(geofences: List<Geofence>): Result<List<Long>> =
        withContext(ioDispatcher) {
            val rowId = geofenceDao.upsertWithTimeStampsUpdate(geofences.map { it.toGeofenceEntity() })
            return@withContext Result.success(rowId)
        }

    override suspend fun addGeofence(
        latLong: LatLong,
        radius: Double,
        name: String,
        bitmap: Bitmap?,
    ): Result<Long> =
        withContext(ioDispatcher) {

            val rowId = geofenceDao.insertOrIgnoreAndUpdateTimestamps(
                GeofenceEntity(
                    id = 0,
                    name = name,
                    latLong = latLong,
                    radius = radius,
                    createdAt = Date(System.currentTimeMillis()),
                    activatedAt = null,
                    bitmapFileName = null,
                    modifiedAt = Date(System.currentTimeMillis())
                )
            )
            if (rowId == -1L) {
                // Unlikely here
                return@withContext Result.failure(Exception("Geofence already exists"))
            } else {
                if (bitmap != null) {
                    geofenceDao.getGeofenceEntity(rowId).first().also { entity ->
                        geofenceFileDao.saveGeofenceImage(bitmap, entity.id).onSuccess {
                            geofenceDao.upsertGeofenceEntities(
                                listOf(entity.copy(bitmapFileName = it))
                            )
                        }
                    }
                }
                return@withContext Result.success(rowId)
            }
        }

    override suspend fun addGeofenceLog(geofenceLogs: List<GeofenceLog>): Result<List<Long>> = withContext(ioDispatcher) {
        val rowIds = geofenceLogDao.insertGeofenceLogs(geofenceLogs.map { it.toGeofenceLogEntity() })
        return@withContext Result.success(rowIds)
    }

    override suspend fun addGeofenceLog(geofenceLog: GeofenceLog): Result<Long> = withContext(ioDispatcher) {
        val rowId = geofenceLogDao.insertGeofenceLog(geofenceLog.toGeofenceLogEntity())
        if (rowId == -1L) {
            return@withContext Result.failure(Exception("GeofenceLog already exists"))
        } else {
            return@withContext Result.success(rowId)
        }
    }

    override fun getGeofenceLogs(geofenceId: Long): Flow<Result<List<GeofenceLog>>> {
        return geofenceLogDao.getGeofenceLogs(geofenceId).map {
            Result.success(it.map { entity -> entity.toGeofenceLog() })
        }
    }

    override suspend fun removeGeofenceLogs(id: Long): Result<Unit> = withContext(ioDispatcher) {
        val deleted = geofenceLogDao.deleteGeofenceLogs(id)
        if (deleted > 0) {
            return@withContext Result.success(Unit)
        } else{
            return@withContext Result.failure(Exception("Return value is not > 0 but, $deleted"))
        }
    }

    override suspend fun saveLastLocation(it: LatLong) {
        withContext(ioDispatcher) {
            geofenceDatastore.insertLocationLatLong(it)
        }
    }

    override fun getLastLocation(): Flow<Result<LatLong>> {
        return geofenceDatastore.getLocationLatLong().map {
            if (it != null) {
                Result.success(it)
            } else {
                Result.failure(NullPointerException("Location is null"))
            }
        }
    }
}