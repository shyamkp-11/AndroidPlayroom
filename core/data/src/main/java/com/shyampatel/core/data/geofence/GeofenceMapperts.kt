package com.shyampatel.core.data.geofence

import com.shyampatel.core.common.geofence.Geofence
import com.shyampatel.core.common.geofence.GeofenceLog
import com.shyampatel.database.geofence.GeofenceEntity
import com.shyampatel.database.geofence.GeofenceLogEntity

fun Geofence.toGeofenceEntity() = GeofenceEntity(
    id = id,
    name = name,
    latLong = latLong,
    radius = radius,
    createdAt = createdAt,
    activatedAt = activatedAt,
    bitmapFileName = bitmapFilePath,
    modifiedAt = modifiedAt
)

fun GeofenceEntity.toGeofence() = Geofence(
    id = id,
    name = name,
    latLong = latLong,
    radius = radius,
    createdAt = createdAt,
    activatedAt = activatedAt,
    bitmapFilePath = bitmapFileName,
    modifiedAt = modifiedAt
)

fun GeofenceLogEntity.toGeofenceLog() = GeofenceLog(
    id = id,
    geofenceEvent = geofenceEvent,
    timeStamp = timeStamp
)

fun GeofenceLog.toGeofenceLogEntity() = GeofenceLogEntity(
    id = id,
    geofenceEvent = geofenceEvent,
    timeStamp = timeStamp
)