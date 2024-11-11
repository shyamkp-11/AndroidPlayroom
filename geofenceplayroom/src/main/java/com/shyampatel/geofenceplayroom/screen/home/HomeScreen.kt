package com.shyampatel.geofenceplayroom.screen.home

import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.shyampatel.geofenceplayroom.R
import com.shyampatel.geofenceplayroom.utils.angleFromCoordinate
import com.shyampatel.geofenceplayroom.utils.calculateScreenWidthDistance
import com.shyampatel.geofenceplayroom.utils.computeDistanceBetween
import com.shyampatel.geofenceplayroom.utils.getCircleRadiusMarkerLatLng
import com.shyampatel.geofenceplayroom.utils.getDistanceString
import com.shyampatel.geofenceplayroom.utils.getMidPoint
import com.shyampatel.geofenceplayroom.utils.getSnapshotZoomLevel
import com.shyampatel.ui.AndroidPlayroomTopAppBar
import com.shyampatel.ui.util.px
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun HomeScreenRoute(
    modifier: Modifier = Modifier,
    navigateToViewGeofences: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
    drawerState: DrawerState,
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val location by homeViewModel.locations.collectAsStateWithLifecycle(initialValue = null)
    val navigate by homeViewModel.navigateToViewGeofences.collectAsStateWithLifecycle()
    BackHandler(uiState is HomeViewModel.HomeState.AddGeoFenceScreen
            || uiState is HomeViewModel.HomeState.GeoFenceLocationSelected) {
        homeViewModel.backPress()
    }
    if (navigate) {
        LaunchedEffect(Unit) {
            navigateToViewGeofences()
            homeViewModel.onViewGeofencesNavigated()
        }
    }
    HomeScreen(
        drawerState = drawerState,
        modifier = modifier,
        uiState = uiState,
        location = location,
        mapLoaded = homeViewModel::mapLoaded,
        createGeoFenceClicked = homeViewModel::createGeoFenceClicked,
        prepareGeofenceClicked = homeViewModel::prepareGeoFenceClicked,
        saveGeoFenceClicked = homeViewModel::saveGeoFenceClicked,
        onMapClick = homeViewModel::onMapClick,
        onCameraMoveFinished = homeViewModel::onCameraMoveFinished,
        onRadiusDrag = homeViewModel::onRadiusDrag,
        onRadiusDragEnd = homeViewModel::onRadiusDragEnd,
        onSaveFenceScreenDismiss = homeViewModel::onSaveFenceScreenDismiss,
        onDialogMapLoaded = homeViewModel::onDialogMapLoaded,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    drawerState: DrawerState,
    uiState: HomeViewModel.HomeState,
    location: Location?,
    mapLoaded: () -> Unit,
    createGeoFenceClicked: () -> Unit,
    prepareGeofenceClicked: () -> Unit,
    saveGeoFenceClicked: (name: String, bitmap: Bitmap?) -> Boolean,
    onMapClick: (center: LatLng, radiusLatLng: LatLng) -> HomeViewModel.MapClickResult,
    onCameraMoveFinished: () -> Unit,
    onRadiusDrag: (latLng: LatLng, diameterToScreenWidthRatio: Double) -> Unit,
    onRadiusDragEnd: () -> LatLng,
    onSaveFenceScreenDismiss: () -> Unit,
    onDialogMapLoaded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AndroidPlayroomTopAppBar(
                drawerState = drawerState,
                titleRes = R.string.app_name,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            MapScreen(
                scope = scope,
                snackbarHostState = snackbarHostState,
                uiState = uiState,
                location = location,
                mapLoaded = mapLoaded,
                createGeoFenceClicked = createGeoFenceClicked,
                prepareGeofenceClicked = prepareGeofenceClicked,
                saveGeoFenceClicked = saveGeoFenceClicked,
                onMapClick = onMapClick,
                onCameraMoveFinished = onCameraMoveFinished,
                onRadiusDrag = onRadiusDrag,
                onRadiusDragEnd = onRadiusDragEnd,
                onSaveFenceScreenDismiss = onSaveFenceScreenDismiss,
                onDialogMapLoaded = onDialogMapLoaded,
            )
        }
    }
}

@Composable
fun MapScreen(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    uiState: HomeViewModel.HomeState,
    location: Location?,
    mapLoaded: () -> Unit,
    createGeoFenceClicked: () -> Unit,
    prepareGeofenceClicked: () -> Unit,
    saveGeoFenceClicked: (name: String, bitmap: Bitmap?) -> Boolean,
    onMapClick: (center: LatLng, radiusLatLng: LatLng) -> HomeViewModel.MapClickResult,
    onCameraMoveFinished: () -> Unit,
    onRadiusDrag: (latLng: LatLng, diameterToScreenWidthRatio: Double) -> Unit,
    onRadiusDragEnd: () -> LatLng,
    onSaveFenceScreenDismiss: () -> Unit,
    onDialogMapLoaded: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        var isDragging by remember { mutableStateOf(false) }
        GeofenceGoogleMap(
            uiState = uiState,
            location = location,
            onCameraMoveFinished = onCameraMoveFinished,
            mapLoaded = mapLoaded,
            onMapClick = onMapClick,
            onRadiusDrag = onRadiusDrag,
            onRadiusDragEnd = onRadiusDragEnd,
            isDragging = { isDragging = it }
        )
        AnimatedVisibility(
            modifier = Modifier
                .matchParentSize(),
            visible = uiState is HomeViewModel.HomeState.MapLoading,
            enter = EnterTransition.None,
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
                    .wrapContentSize()
            )
        }
        when (uiState) {
            is HomeViewModel.HomeState.MapLoading -> {
                // Nothing to do here. Animation takes care of it.
            }

            is HomeViewModel.HomeState.ShowCreateGeoFenceButton -> {
                MapButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    text = "Create Geofence",
                    onClick = { createGeoFenceClicked() }
                )
            }

            is HomeViewModel.HomeState.AddGeoFenceScreen -> {
                LaunchedEffect(key1 = uiState) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Click on the map to create a geofence"
                        )
                    }
                }
            }

            is HomeViewModel.HomeState.GeoFenceLocationSelected -> {
                if (!isDragging) {
                    MapButton(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        text = "Add Geofence",
                        onClick = prepareGeofenceClicked
                    )
                }
            }

            is HomeViewModel.HomeState.SaveGeoFenceScreen -> {
                GeofenceConfirmationDialog(
                    onDismissRequest = onSaveFenceScreenDismiss,
                    onConfirmation = saveGeoFenceClicked,
                    error = "",
                    uiState = uiState,
                    onDialogMapLoaded = onDialogMapLoaded,
                )
            }
        }
    }
}

@Composable
private fun GeofenceGoogleMap(
    uiState: HomeViewModel.HomeState,
    location: Location?,
    onCameraMoveFinished: () -> Unit,
    mapLoaded: () -> Unit,
    onMapClick: (center: LatLng, radiusLatLng: LatLng) -> HomeViewModel.MapClickResult,
    onRadiusDrag: (latLng: LatLng, diameterToScreenWidthRatio: Double) -> Unit,
    onRadiusDragEnd: () -> LatLng,
    isDragging: (isDragging: Boolean) -> Unit,
) {
    val locationSource by remember { mutableStateOf(MyLocationSource()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(38.79, -103.5), 4f)
    }

    LaunchedEffect(key1 = location) {
        if (location != null) {
            locationSource.onLocationChanged(location)
        }
    }
    LaunchedEffect(key1 = uiState.mapCamera) {
        if (uiState.mapCamera.zoomOrLatLngBounds.first != null) {
            val cameraPosition =
                CameraPosition.fromLatLngZoom(
                    uiState.mapCamera.zoomOrLatLngBounds.first!!.first,
                    uiState.mapCamera.zoomOrLatLngBounds.first!!.second
                )
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                1_000
            )
        } else if (uiState.mapCamera.zoomOrLatLngBounds.second != null) {

            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                uiState.mapCamera.zoomOrLatLngBounds.second!!,
                150
            )
            cameraPositionState.animate(cameraUpdate)
        }
        onCameraMoveFinished()
    }

    val configuration: Configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp.px
    val screenWidth = configuration.screenWidthDp.dp.px

    val mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = true)) }
    val mapUiSettings by remember(uiState) {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = uiState is HomeViewModel.HomeState.ShowCreateGeoFenceButton
            )
        )
    }
    var waypoints by rememberSaveable {
        mutableStateOf<List<LatLng>>(
            listOf()
        )
    }
    val midPoint = rememberMarkerState()
    GoogleMap(
        modifier = Modifier.fillMaxSize(
        ),
        onMapLoaded = {
            mapLoaded()
        },
        uiSettings = mapUiSettings,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        locationSource = locationSource,
        onMapClick = { position ->
            cameraPositionState.projection?.let {
                val radiusLatLng = getCircleRadiusMarkerLatLng(
                    it,
                    position,
                    screenWidth.toInt(),
                    screenHeight.toInt()
                )
                val mapClickResult = onMapClick(position, radiusLatLng)
                if (mapClickResult is HomeViewModel.MapClickResult.RadiusLatLng) {
                    midPoint.position = getMidPoint(position, mapClickResult.radiusLatLng)
                    waypoints = listOf(position, mapClickResult.radiusLatLng)
                }
            }
        }
    ) {
        if (uiState is HomeViewModel.HomeState.GeoFenceLocationSelected) {
            MarkerComposable(
                state = rememberMarkerState(position = uiState.latLng),
                title = "Geofence center"
            ) {
                Icon(
                    painter = painterResource(R.drawable.thumbtack),
                    contentDescription = "Geofence pin icon",
                    tint = Color.Blue,
                    modifier = Modifier
                        .size(42.dp)
                        .offset(y = (4).dp),
                )
            }
            Circle(
                center = uiState.latLng,
                radius = uiState.radius,
                strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                strokeWidth = 8f,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )
            MarkerComposable(
                keys = arrayOf(uiState.radius),
                state = midPoint,
                rotation = ((angleFromCoordinate(
                    uiState.latLng,
                    uiState.dragMarkerLatLng
                ).toFloat() + 270f) % 360f).let { it1 ->
                    if (it1 < 90 || it1 > 270) {
                        it1
                    } else {
                        it1 + 180
                    }
                }
            ) {
                Text(
                    text = getDistanceString(uiState.radius),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
            Polyline(
                points = waypoints,
                pattern = listOf(Dash(10F), Gap(13F)),
                startCap = RoundCap(),
                endCap = RoundCap(),
                width = 5f,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            )
            val dragMarkerState = rememberMarkerState(position = uiState.dragMarkerLatLng)
            DraggableMarker(
                markerState = dragMarkerState,
                title = "Drag to change radius",
                tint = MaterialTheme.colorScheme.primary,
                painter = painterResource(R.drawable.thumbtack),
                modifier = Modifier
                    .size(32.dp)
                    .offset(y = (4).dp),
                onDragStart = {
                    isDragging(true)
                },
                onDrag = {
                    cameraPositionState.projection?.let { proj ->
                        val screenDistance =
                            calculateScreenWidthDistance(proj.visibleRegion.latLngBounds)
                        val radius = computeDistanceBetween(uiState.latLng, it)
                        waypoints = listOf(uiState.latLng, it)
                        midPoint.position = getMidPoint(uiState.latLng, it)
                        onRadiusDrag(it, 2 * radius / screenDistance)
                    }
                },
                onDragEnd = {
                    isDragging(false)
                    val dragMarkerLatLng = onRadiusDragEnd()
                    waypoints = listOf(uiState.latLng, dragMarkerLatLng)
                    midPoint.position = getMidPoint(uiState.latLng, dragMarkerLatLng)
                    dragMarkerState.position = dragMarkerLatLng
                },
            )
        }
    }
}

@Composable
fun GeofenceConfirmationDialog(
    uiState: HomeViewModel.HomeState,
    onDismissRequest: () -> Unit,
    onConfirmation: (name: String, bitmap: Bitmap?) -> Boolean,
    error: String,
    onDialogMapLoaded: () -> Unit,
) {
    var takeSnapShot by remember { mutableStateOf(false) }
    var mapSnapshot by remember { mutableStateOf<Bitmap?>(null) }

    var text by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Add Geofence", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                if (uiState is HomeViewModel.HomeState.SaveGeoFenceScreen) {
                    val mapCameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            uiState.latLng,
                            getSnapshotZoomLevel(uiState.radius)
                        )
                    }
                    GoogleMapViewInColumn(
                        uiState = uiState,
                        modifier = Modifier
                            .height(300.dp)
                            .padding(horizontal = 16.dp)
                            .clip(shape = RoundedCornerShape(16.dp)),
                        cameraPositionState = mapCameraPositionState,
                        onMapLoaded = {
                            onDialogMapLoaded()
                        },
                        takeSnapShot = takeSnapShot,
                        updateSnapshot = {
                            takeSnapShot = false
                            mapSnapshot = it
                            onConfirmation(text, it)
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (error.isNotEmpty()) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedTextField(
                        value = text,
                        label = { Text(text = "Geofence name") },
                        onValueChange = { newText: String ->
                            text = newText.trimStart()
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        isError = error.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = { onDismissRequest() },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                        TextButton(
                            onClick = {
                                takeSnapShot = true
                            },
                            enabled = uiState.enableSave,
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Enable Geofence")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun GoogleMapViewInColumn(
    uiState: HomeViewModel.HomeState.SaveGeoFenceScreen,
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    onMapLoaded: () -> Unit,
    takeSnapShot: Boolean,
    updateSnapshot: (Bitmap) -> Unit,
) {

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                scrollGesturesEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false,
            )
        )
    }
    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapLoaded = onMapLoaded,
    ) {
        Circle(
            center = uiState.latLng,
            radius = uiState.radius,
            strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            strokeWidth = 8f,
            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )
        if (takeSnapShot) {
            MapEffect(key1 = uiState.latLng) {
                it.snapshot { snapshot ->
                    if (snapshot != null) {
                        updateSnapshot(snapshot)
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableMarker(
    markerState: MarkerState,
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit = {},
    onDrag: (LatLng) -> Unit = {},
    onDragEnd: () -> Unit = {},
    title: String? = null,
    tint: Color,
    painter: Painter,
) {
    MarkerComposable(
        state = markerState, draggable = true,
        zIndex = 0.0f, flat = true,
        title = title
    ) {
        Icon(
            painter = painter,
            contentDescription = "Geofence radius pin icon",
            tint = tint,
            modifier = modifier,
        )
    }

    LaunchedEffect(Unit) {
        var inDrag = false
        var priorPosition: LatLng? = markerState.position
        snapshotFlow { markerState.isDragging to markerState.position }
            .dropWhile { (isDragging, position) ->
                !isDragging && position == priorPosition // ignore initial value
            }
            .collect { (isDragging, position) ->
                // Do not even bother to check isDragging state here:
                // it is possible to miss a sequence of states
                // where isDragging == true, then isDragging == false;
                // in this case we would only see a change in position.
                // (Hypothetically we could even miss a change in position
                // if the Marker ended up in its original position at the
                // end of the drag. But then nothing changed at all,
                // so we should be ok to ignore this case altogether.)
                if (!inDrag) {
                    inDrag = true
                    onDragStart()
                }

                if (position != priorPosition) {
                    onDrag(position)
                    priorPosition = position
                }

                if (!isDragging) {
                    inDrag = false
                    onDragEnd()
                }
            }
    }
}

private class MyLocationSource : LocationSource {

    private var listener: OnLocationChangedListener? = null

    override fun activate(listener: OnLocationChangedListener) {
        this.listener = listener
    }

    override fun deactivate() {
        listener = null
    }

    fun onLocationChanged(location: Location) {
        listener?.onLocationChanged(location)
    }
}

@Composable
private fun MapButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.onPrimary,
        ),
        onClick = onClick
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

