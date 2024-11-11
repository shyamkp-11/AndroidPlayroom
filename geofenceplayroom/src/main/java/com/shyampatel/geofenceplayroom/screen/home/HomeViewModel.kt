package com.shyampatel.geofenceplayroom.screen.home

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.shyampatel.core.common.geofence.LatLong
import com.shyampatel.core.data.geofence.GeofenceRepository
import com.shyampatel.geofenceplayroom.GeofenceManager
import com.shyampatel.geofenceplayroom.repository.LocationRepository
import com.shyampatel.geofenceplayroom.utils.computeDistanceBetween
import com.shyampatel.geofenceplayroom.utils.computeOffset
import com.shyampatel.geofenceplayroom.utils.getCircleBounds
import com.shyampatel.ui.ErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val locationRepository: LocationRepository,
    private val geofenceRepository: GeofenceRepository,
    private val geofenceManager: GeofenceManager,
) : ViewModel() {

    private val _navigateToViewGeofences = MutableStateFlow(false)
    val navigateToViewGeofences = _navigateToViewGeofences.asStateFlow()

    private val viewModelState = MutableStateFlow(
        HomeViewModelState(
            firstMyLocationFetched = false,
            stopCameraToMyLocation = false,
            isLoading = false,
            errorMessages = emptyList(),
            screenState = ScreenState.MapLoading,
            mapCamera = MapCamera((LatLng(38.79, -103.5) to 4f) to null)
        )
    )

    val uiState: StateFlow<HomeState> = viewModelState.map {
        it.toUiState()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = viewModelState.value.toUiState()
    )

    val locations = merge(
        geofenceRepository.getLastLocation().map { result ->
            val location = result.getOrNull()?.let { latlng ->
                Location("").apply { latitude = latlng.latitude; longitude = latlng.longitude }
            }
            Result.success(location)
        }.take(1),
        locationRepository.getLocations().withIndex().map {
            // saving all updates for now
            if (true) {
                it.value.getOrNull()?.let { location ->
                    viewModelScope.launch {
                        geofenceRepository.saveLastLocation(
                            LatLong(
                                location.latitude,
                                location.longitude
                            )
                        )
                    }
                }
            }
            it.value
        }).map { result: Result<Location?> -> result.getOrNull() }
        .map { location: Location? ->
            if (location == null) return@map null
            if (!viewModelState.value.stopCameraToMyLocation) {
                viewModelState.update {
                    it.copy(
                        firstMyLocationFetched = true,
                        mapCamera = MapCamera(
                            (LatLng(location.latitude, location.longitude) to 14f) to null
                        )
                    )
                }
            }
            location
        }.shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000)
        )

    /**
     * Returns true if no validation errors / event consumed.
     */
    fun saveGeoFenceClicked(name: String, bitmap: Bitmap?): Boolean {
        if (viewModelState.value.screenState is ScreenState.SaveGeoFenceScreen) {
            if (name.length !in 2..50) {
                // Todo add validations errors
                return false
            } else {
                val screenState = viewModelState.value.screenState as ScreenState.SaveGeoFenceScreen
                viewModelState.update {
                    it.copy(isLoading = true)
                }
                viewModelScope.launch {
                    geofenceRepository.addGeofence(
                        latLong = LatLong(
                            screenState.latLng.latitude,
                            screenState.latLng.longitude
                        ),
                        radius = screenState.radius,
                        name = name,
                        bitmap = bitmap,
                    ).onSuccess { rowId ->
                        geofenceRepository.getGeofenceForRowId(rowId).first().onSuccess {
                            geofenceManager.addGeofence(
                                key = it.id.toString(),
                                location = Location(null).apply {
                                    latitude = it.latLong.latitude; longitude = it.latLong.longitude
                                },
                                radiusInMeters = screenState.radius.toFloat(),
                                expirationTimeInMillis = 30 * 24 * 60 * 60 * 1000L,
                            )
                            geofenceManager.registerGeofence()
                            geofenceRepository.upsertGeofences(
                                listOf(
                                    it.copy(
                                        activatedAt = Date(System.currentTimeMillis())
                                    )
                                )
                            )
                            viewModelState.update { state ->
                                state.copy(
                                    screenState = ScreenState.ShowCreateGeofence,
                                    isLoading = false
                                )
                            }
                            _navigateToViewGeofences.value = true
                        }
                    }
                }
            }
        }
        return true
    }

    /**
     * Returns true if event consumed
     */
    fun onMapClick(centerLatLng: LatLng, radiusLatLng: LatLng): MapClickResult {
        if (viewModelState.value.screenState is ScreenState.AddGeoFenceScreen) {
            val radius = computeDistanceBetween(centerLatLng, radiusLatLng)
            val radiusLatLngAndRadius = if (radius < 120) {
                computeOffset(
                    centerLatLng,
                    120.0,
                    90.0
                ) to 120.0
            } else if (radius > 10000) {
                computeOffset(
                    centerLatLng,
                    10000.0,
                    90.0
                ) to 10000.0
            } else {
                radiusLatLng to radius
            }
            viewModelState.update {
                it.copy(
                    screenState = ScreenState.GeofenceLocationSelected(
                        latLng = centerLatLng,
                        dragMarkerLatLng = radiusLatLngAndRadius.first,
                        radius = radiusLatLngAndRadius.second,
                    ),
                    mapCamera = MapCamera(
                        null to getCircleBounds(
                            centerLatLng,
                            radiusLatLngAndRadius.second
                        )
                    ),
                    stopCameraToMyLocation = true
                )
            }
            return MapClickResult.RadiusLatLng(radiusLatLngAndRadius.first)
        }
        return MapClickResult.UnConsumed
    }

    fun onRadiusDrag(latLng: LatLng, diameterToScreenWidthRatio: Double) {
        val screenState = viewModelState.value.screenState as ScreenState.GeofenceLocationSelected
        val radius = computeDistanceBetween(screenState.latLng, latLng)
        viewModelState.update {
            it.copy(
                screenState = ScreenState.GeofenceLocationSelected(
                    latLng = screenState.latLng,
                    dragMarkerLatLng = latLng,
                    radius = radius,
                ),
                mapCamera = if (diameterToScreenWidthRatio < 0.3 || diameterToScreenWidthRatio > 0.8) {
                    MapCamera(null to getCircleBounds(screenState.latLng, radius))
                } else {
                    it.mapCamera
                }
            )
        }
    }

    fun onRadiusDragEnd(): LatLng {
        val screenState = viewModelState.value.screenState as ScreenState.GeofenceLocationSelected
        if (screenState.radius < 120) {
            val latLng = computeOffset(
                (viewModelState.value.screenState as ScreenState.GeofenceLocationSelected).latLng,
                120.0,
                90.0
            )
            viewModelState.update {
                it.copy(
                    screenState = ScreenState.GeofenceLocationSelected(
                        latLng = (it.screenState as ScreenState.GeofenceLocationSelected).latLng,
                        dragMarkerLatLng = latLng,
                        radius = 120.0,
                    ),
                    mapCamera = MapCamera(null to getCircleBounds(screenState.latLng, 120.0))
                )
            }
            return latLng
        } else if (screenState.radius > 10000) {
            val latLng = computeOffset(
                (viewModelState.value.screenState as ScreenState.GeofenceLocationSelected).latLng,
                10000.0,
                90.0
            )
            viewModelState.update {
                it.copy(
                    screenState = ScreenState.GeofenceLocationSelected(
                        latLng = (it.screenState as ScreenState.GeofenceLocationSelected).latLng,
                        dragMarkerLatLng = latLng,
                        radius = 10000.0,
                    ),
                    mapCamera = MapCamera(null to getCircleBounds(screenState.latLng, 10000.0))
                )
            }
            return latLng
        }
        return screenState.dragMarkerLatLng
    }

    fun mapLoaded() {
        if (viewModelState.value.screenState is ScreenState.MapLoading)
            viewModelState.update {
                it.copy(screenState = ScreenState.ShowCreateGeofence)
            }
    }

    fun createGeoFenceClicked() {
        viewModelState.update {
            it.copy(screenState = ScreenState.AddGeoFenceScreen)
        }
    }

    fun prepareGeoFenceClicked() {
        viewModelState.update {
            it.copy(
                screenState = ScreenState.SaveGeoFenceScreen(
                    latLng = (it.screenState as ScreenState.GeofenceLocationSelected).latLng,
                    radius = (it.screenState as ScreenState.GeofenceLocationSelected).radius,
                    enableSave = false,
                )
            )
        }
    }


    fun onDialogMapLoaded() {
        if (viewModelState.value.screenState is ScreenState.SaveGeoFenceScreen) {
            viewModelState.update {
                it.copy(
                    screenState = ScreenState.SaveGeoFenceScreen(
                        latLng = (it.screenState as ScreenState.SaveGeoFenceScreen).latLng,
                        radius = (it.screenState as ScreenState.SaveGeoFenceScreen).radius,
                        enableSave = true,
                    )
                )
            }
        }
    }

    fun backPress() {
        if (viewModelState.value.screenState is ScreenState.GeofenceLocationSelected ||
            viewModelState.value.screenState is ScreenState.AddGeoFenceScreen
        ) {
            viewModelState.update {
                it.copy(screenState = ScreenState.ShowCreateGeofence)
            }
        }
    }

    fun onSaveFenceScreenDismiss() {
        viewModelState.update {
            it.copy(screenState = ScreenState.ShowCreateGeofence)
        }
    }

    fun onCameraMoveFinished() {
        if (viewModelState.value.firstMyLocationFetched && !viewModelState.value.stopCameraToMyLocation) {
            viewModelState.update {
                it.copy(stopCameraToMyLocation = true)
            }
        }
    }

    fun onViewGeofencesNavigated() {
        _navigateToViewGeofences.value = false
    }

    data class MapCamera(val zoomOrLatLngBounds: Pair<Pair<LatLng, Float>?, LatLngBounds?>)

    sealed interface MapClickResult {
        data class RadiusLatLng(val radiusLatLng: LatLng) : MapClickResult
        data object UnConsumed : MapClickResult
    }

    sealed interface HomeState {
        val isLoading: Boolean
        val errorMessages: List<ErrorMessage>
        val mapCamera: MapCamera

        data class MapLoading(
            override val isLoading: Boolean,
            override val errorMessages: List<ErrorMessage>,
            override val mapCamera: MapCamera,
        ) : HomeState

        data class ShowCreateGeoFenceButton(
            override val isLoading: Boolean,
            override val errorMessages: List<ErrorMessage>,
            override val mapCamera: MapCamera,
        ) : HomeState

        data class AddGeoFenceScreen(
            override val isLoading: Boolean,
            override val errorMessages: List<ErrorMessage>,
            override val mapCamera: MapCamera,
        ) : HomeState

        data class SaveGeoFenceScreen(
            val latLng: LatLng,
            val radius: Double,
            val enableSave: Boolean,
            override val isLoading: Boolean,
            override val errorMessages: List<ErrorMessage>,
            override val mapCamera: MapCamera,
        ) : HomeState

        data class GeoFenceLocationSelected(
            val latLng: LatLng,
            val radius: Double,
            val dragMarkerLatLng: LatLng,
            override val mapCamera: MapCamera,
            override val isLoading: Boolean,
            override val errorMessages: List<ErrorMessage>,
        ) : HomeState
    }

    sealed interface ScreenState {
        data object MapLoading : ScreenState
        data object ShowCreateGeofence : ScreenState
        data object AddGeoFenceScreen : ScreenState
        data class SaveGeoFenceScreen(
            val latLng: LatLng,
            val radius: Double,
            val enableSave: Boolean,
        ) : ScreenState

        data class GeofenceLocationSelected(
            val latLng: LatLng,
            val radius: Double,
            val dragMarkerLatLng: LatLng,
        ) : ScreenState
    }

    private data class HomeViewModelState(
        val firstMyLocationFetched: Boolean,
        val stopCameraToMyLocation: Boolean,
        val isLoading: Boolean,
        val errorMessages: List<ErrorMessage> = emptyList(),
        val screenState: ScreenState,
        val mapCamera: MapCamera,
    ) {
        fun toUiState(): HomeState = when (this.screenState) {
            ScreenState.MapLoading -> HomeState.MapLoading(
                isLoading = isLoading,
                errorMessages = errorMessages,
                mapCamera = mapCamera,
            )

            ScreenState.ShowCreateGeofence -> HomeState.ShowCreateGeoFenceButton(
                isLoading = isLoading,
                errorMessages = errorMessages,
                mapCamera = mapCamera,
            )

            ScreenState.AddGeoFenceScreen -> HomeState.AddGeoFenceScreen(
                isLoading = isLoading,
                errorMessages = errorMessages,
                mapCamera = mapCamera
            )

            is ScreenState.GeofenceLocationSelected -> HomeState.GeoFenceLocationSelected(
                isLoading = isLoading,
                errorMessages = errorMessages,
                latLng = this.screenState.latLng,
                mapCamera = mapCamera,
                dragMarkerLatLng = this.screenState.dragMarkerLatLng,
                radius = this.screenState.radius,
            )

            is ScreenState.SaveGeoFenceScreen -> HomeState.SaveGeoFenceScreen(
                isLoading = isLoading,
                errorMessages = errorMessages,
                mapCamera = mapCamera,
                latLng = this.screenState.latLng,
                radius = this.screenState.radius,
                enableSave = this.screenState.enableSave,
            )
        }
    }
}