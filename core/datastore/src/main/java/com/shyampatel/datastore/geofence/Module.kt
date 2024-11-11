package com.shyampatel.datastore.geofence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shyampatel.datastore.userPreferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun getFileDataStoreModule(applicationContext: Context, ioDispatcher: CoroutineDispatcher) = module {
    factory<GeofenceFileDao> {
        GeofenceFileDaoImpl(filesDirAbsPath = applicationContext.filesDir.absolutePath)
    }
}
fun getGeofenceDataStoreModule(applicationContext: Context, ioDispatcher: CoroutineDispatcher) = module {
    single<DataStore<Preferences>>(named("geofencePreferencesDataStore")) { applicationContext.geofencePreferencesDatastore }
    factory<GeofenceDatastore> {
        GeofenceDataStoreImpl(
            geofencePreferencesDataStore = get(named("geofencePreferencesDataStore")),
            ioDispatcher = ioDispatcher
        )
    }
}