package com.shyampatel.geofenceplayroom.repository

class LocationRepository(
        private val sharedLocationManager: SharedLocationManager
) {
    fun getLocations() = sharedLocationManager.locationFlow()
}