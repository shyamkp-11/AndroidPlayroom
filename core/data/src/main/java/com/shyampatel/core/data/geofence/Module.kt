package com.shyampatel.core.data.geofence

import android.content.Context
import com.shyampatel.core.data.permissions.PermissionsRepository
import com.shyampatel.core.data.permissions.PermissionsRepositoryImpl
import com.shyampatel.database.geofence.getDatabaseModule
import com.shyampatel.datastore.geofence.getFileDataStoreModule
import com.shyampatel.datastore.geofence.getGeofenceDataStoreModule
import com.shyampatel.datastore.permissions.getPermissionsDataStoreModule
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.dsl.module

fun getDataModule(applicationContext: Context,
                   defaultDispatcher: CoroutineDispatcher,
                   ioDispatcher: CoroutineDispatcher) = module {
    includes(
        getPermissionsDataStoreModule(applicationContext = applicationContext, ioDispatcher = ioDispatcher),
        getFileDataStoreModule(applicationContext = applicationContext, ioDispatcher = ioDispatcher),
        getDatabaseModule(applicationContext),
        getGeofenceDataStoreModule(applicationContext = applicationContext, ioDispatcher = ioDispatcher)
    )
    factory<GeofenceRepository>{
        GeofenceRepositoryImpl(
            ioDispatcher = ioDispatcher,
            geofenceDao = get(),
            geofenceFileDao = get(),
            geofenceLogDao = get(),
            geofenceDatastore = get()
        )
    }
    factory<PermissionsRepository>{
        PermissionsRepositoryImpl(
            dataStore = get(),
            ioDispatcher = ioDispatcher
        )
    }
}