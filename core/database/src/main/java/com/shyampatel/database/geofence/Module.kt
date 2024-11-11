package com.shyampatel.database.geofence

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module

fun getDatabaseModule(applicationContext: Context) = module {
    single<GeofenceDatabase> {
        Room.databaseBuilder(
            applicationContext,
            GeofenceDatabase::class.java,
            "geofence-repo-database",
        ).build()
    }
    factory<GeofenceRepoDao> {
        get<GeofenceDatabase>().geofenceDao()
    }

    factory<GeofenceLogDao> {
        get<GeofenceDatabase>().geofenceLogDao()
    }
}