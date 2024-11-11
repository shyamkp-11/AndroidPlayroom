package com.shyampatel.database.geofence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GeofenceEntity::class, GeofenceLogEntity::class] ,
    version = 3
)
@TypeConverters(
    DateTypeConverters::class,
    GeofenceEventTypeConverters::class
)
internal abstract class GeofenceDatabase: RoomDatabase() {
    abstract fun geofenceDao(): GeofenceRepoDao
    abstract fun geofenceLogDao(): GeofenceLogDao
}