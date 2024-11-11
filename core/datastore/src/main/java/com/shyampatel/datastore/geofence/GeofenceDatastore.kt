package com.shyampatel.datastore.geofence

import com.shyampatel.core.common.geofence.LatLong
import kotlinx.coroutines.flow.Flow

interface GeofenceDatastore {
    suspend fun insertLocationLatLong(latLong: LatLong)
    fun getLocationLatLong(): Flow<LatLong?>
}