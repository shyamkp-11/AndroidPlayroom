package com.shyampatel.datastore.geofence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.shyampatel.core.common.geofence.LatLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GeofenceDataStoreImpl(
    val geofencePreferencesDataStore: DataStore<Preferences>,
    val ioDispatcher: CoroutineDispatcher
): GeofenceDatastore {
    private val LATITUDE by lazy { doublePreferencesKey("latitude") }
    private val LONGITUDE by lazy { doublePreferencesKey("longitude") }

    override suspend fun insertLocationLatLong(latLong: LatLong) {
        withContext(ioDispatcher) {
            geofencePreferencesDataStore.edit {
                it[LATITUDE] = latLong.latitude
                it[LONGITUDE] = latLong.longitude
            }
        }
    }

    override fun getLocationLatLong(): Flow<LatLong?> {
        return geofencePreferencesDataStore.data.map {
            it[LATITUDE]?.let { latitude ->
                it[LONGITUDE]?.let { longitude ->
                    LatLong(latitude, longitude)
                }
            }
        }
    }
}