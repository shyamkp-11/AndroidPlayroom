package com.shyampatel.database.geofence

import androidx.room.Entity
import com.shyampatel.core.common.geofence.GeofenceEvent

@Entity(
    tableName = "geofence_log_entity",
    primaryKeys = ["id", "geofenceEvent", "timeStamp"]
)
data class GeofenceLogEntity(val id: Long, val geofenceEvent: GeofenceEvent, val timeStamp: Long)