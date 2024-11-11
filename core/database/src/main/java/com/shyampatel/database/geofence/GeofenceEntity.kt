package com.shyampatel.database.geofence

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shyampatel.core.common.geofence.LatLong
import java.util.Date

@Entity(
    tableName = "geofence_entity",
)
data class GeofenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @Embedded
    val latLong: LatLong,
    val radius: Double,
    val createdAt: Date,
    val activatedAt: Date?,
    val bitmapFileName: String?,
    val modifiedAt: Date
)