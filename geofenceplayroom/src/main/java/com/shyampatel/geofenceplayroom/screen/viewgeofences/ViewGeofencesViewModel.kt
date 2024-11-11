package com.shyampatel.geofenceplayroom.screen.viewgeofences

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.common.geofence.Geofence
import com.shyampatel.core.data.geofence.GeofenceRepository
import com.shyampatel.geofenceplayroom.GeofenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class ViewGeofencesViewModel(
    val notificationPermissionGranted: Boolean,
    val geofenceManager: GeofenceManager,
    val geofenceRepository: GeofenceRepository,
): ViewModel() {

    private val _showPermissionsScreen = MutableStateFlow(!notificationPermissionGranted)
    val navigateToPermissionScreen: StateFlow<Boolean> = _showPermissionsScreen.asStateFlow()

    private val _navigateToFenceTriggered = MutableStateFlow<Long?>(null)
    val navigateToFenceTriggered: StateFlow<Long?> = _navigateToFenceTriggered.asStateFlow()

    val myGeofencesScreenState: StateFlow<MyGeofencesScreenState> = load().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyGeofencesScreenState.Loading
    )

    init {

    }

    fun onPermissionGranted() {
        _showPermissionsScreen.value = false
    }

    fun onPermissionsScreenNavigated() {
        _showPermissionsScreen.value = false
    }

    private fun load(): Flow<MyGeofencesScreenState> {
        return geofenceRepository.getGeofences().map {result ->
            if (result.isSuccess){
                result.getOrNull()!!.sortedByDescending { it.activatedAt?.time }.let {
                    MyGeofencesScreenState.Success(it)
                }
            } else {
                Log.e(MyGeofencesScreenState::class.simpleName, result.exceptionOrNull()?.stackTraceToString()?:"")
                MyGeofencesScreenState.Error
            }
        }
    }

    fun onActivatedChanged(isActive: Boolean, id: Long) {
        if (myGeofencesScreenState.value is MyGeofencesScreenState.Success) {
            val geofences = (myGeofencesScreenState.value as MyGeofencesScreenState.Success).geofences
            val currentFence = geofences.first { it.id == id }
            viewModelScope.launch {
                if (isActive) {
                    geofenceManager.addGeofence(
                        key = currentFence.id.toString(),
                        location = Location(null).apply { latitude = currentFence.latLong.latitude; longitude = currentFence.latLong.longitude },
                        radiusInMeters = currentFence.radius.toFloat(),
                        expirationTimeInMillis = 30 * 24 * 60 * 60 * 1000L,
                    )
                    geofenceManager.registerGeofence()
                } else {
                    geofenceManager.deregisterGeofence(id.toString())
                }
                geofenceRepository.upsertGeofences(
                    listOf(
                        currentFence.copy(activatedAt = if (isActive) {
                            Date(System.currentTimeMillis())
                        } else {
                            null
                        }
                        )
                    )
                )
            }
        }
    }

    fun onDeleted(id: Long) {
        if (myGeofencesScreenState.value is MyGeofencesScreenState.Success) {
            viewModelScope.launch {
                geofenceManager.deregisterGeofence(id.toString())
                geofenceRepository.removeGeofence(id = id)
            }
        }
    }

    fun onGeofenceClicked(id: Long) {
        _navigateToFenceTriggered.value = id
    }

    fun onFenceTriggeredNavigated(){
        _navigateToFenceTriggered.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(ViewGeofencesViewModel::class.simpleName, "onCleared")
    }
}

sealed interface MyGeofencesScreenState {
    data class Success(val geofences: List<Geofence>) : MyGeofencesScreenState
    data object Error : MyGeofencesScreenState
    data object Loading : MyGeofencesScreenState
}