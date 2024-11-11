package com.shyampatel.geofenceplayroom.screen.fencetriggered

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.data.geofence.GeofenceRepository
import com.shyampatel.geofenceplayroom.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FenceTriggeredViewModel(
    private val id: Long,
    private val geofenceManager: GeofenceManager,
    private val geofenceRepository: GeofenceRepository
): ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    val getFenceLogs = geofenceRepository.getGeofenceLogs(id).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Result.success(emptyList())
    ).map { if (it.isSuccess) it.getOrThrow().sortedByDescending { log -> log.timeStamp } else emptyList() }

    val getGeofence = geofenceRepository.getGeofence(id).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Result.success(null)
    ).map { if (it.isSuccess) it.getOrThrow() else null }

    fun removeGeofenceLogs(id: Long) {
        viewModelScope.launch {
            geofenceRepository.removeGeofenceLogs(id)
        }
    }

}
