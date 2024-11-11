package com.shyampatel.geofenceplayroom.screen.viewgeofences

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shyampatel.core.common.geofence.Geofence
import com.shyampatel.core.common.geofence.LatLong
import com.shyampatel.geofenceplayroom.R
import com.shyampatel.ui.AndroidPlayroomLoadingIndicator
import com.shyampatel.ui.AndroidPlayroomTopAppBar
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Date

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun ViewGeofencesScreenRoute(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    navigateToFenceTriggered: (id: Long) -> Unit,
    navigateToPermissionsScreen: () -> Unit,
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberMultiplePermissionsState(
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        )
        val viewGeofencesViewModel = koinViewModel<ViewGeofencesViewModel>(
            parameters = { parametersOf(permissionState.allPermissionsGranted) }
        )
        if (permissionState.allPermissionsGranted) {
            viewGeofencesViewModel.onPermissionGranted()
        }
        val navigateToPermissionsScreen by viewGeofencesViewModel.navigateToPermissionScreen.collectAsStateWithLifecycle()
        if (navigateToPermissionsScreen) {
            LaunchedEffect(key1 = Unit) {
                navigateToPermissionsScreen()
                viewGeofencesViewModel.onPermissionsScreenNavigated()
            }
        }

        val state: MyGeofencesScreenState by viewGeofencesViewModel.myGeofencesScreenState.collectAsStateWithLifecycle()
        val navigateToFenceTriggered by viewGeofencesViewModel.navigateToFenceTriggered.collectAsStateWithLifecycle()
        navigateToFenceTriggered?.let {
            LaunchedEffect(key1 = Unit) {
                navigateToFenceTriggered(it)
                viewGeofencesViewModel.onFenceTriggeredNavigated()
            }
        }
        ViewGeofencesScreen(
            drawerState = drawerState,
            state = state,
            onActivatedChanged = viewGeofencesViewModel::onActivatedChanged,
            onDeleted = viewGeofencesViewModel::onDeleted,
            onGeofenceClicked = viewGeofencesViewModel::onGeofenceClicked,
            modifier = modifier
        )

    } else {
        TODO("VERSION.SDK_INT < TIRAMISU")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGeofencesScreen(
    drawerState: DrawerState,
    state: MyGeofencesScreenState,
    onActivatedChanged: (isActive: Boolean, id: Long) -> Unit,
    onDeleted: (id: Long) -> Unit,
    onGeofenceClicked: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    Scaffold(
        topBar = {
            AndroidPlayroomTopAppBar(
                drawerState = drawerState,
                titleRes = R.string.view_geofences_screen_top_bar_title,
                topAppBarState = topAppBarState
            )
        },
        modifier = modifier,
    )
    { innerPadding ->
        val contentModifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        when (state) {
            is MyGeofencesScreenState.Success -> {
                LazyColumn(modifier = contentModifier, contentPadding = innerPadding) {
                    items(
                        items = state.geofences,
                        key = {
                            it.id
                        }
                    ) { task ->
                        ViewGeofenceListItem(
                            geofence = task,
                            onActivatedChanged = onActivatedChanged,
                            onDeleted = onDeleted,
                            onGeofenceClicked = onGeofenceClicked,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            MyGeofencesScreenState.Error -> {}
            MyGeofencesScreenState.Loading -> {
                AndroidPlayroomLoadingIndicator(modifier = modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun ViewGeofenceListItem(
    geofence: Geofence,
    onActivatedChanged: (isActive: Boolean, id: Long) -> Unit,
    onDeleted: (id: Long) -> Unit,
    onGeofenceClicked: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {

    val image = remember {
        if (!geofence.bitmapFilePath.isNullOrEmpty()) {
            BitmapFactory.decodeFile(geofence.bitmapFilePath).asImageBitmap()
        } else {
            null
        }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onGeofenceClicked(geofence.id) }
    ) {
        Column {
            if (image != null) {
                Image(
                    bitmap = image, contentDescription = "Geofence map image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_map),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentDescription = "Geofence map image"
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .weight(4f)
                ) {
                    Text(
                        modifier = Modifier,
                        text = geofence.name,
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = (MaterialTheme.typography.titleMedium.lineHeight.value - 2).sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier,
                        text = remember(geofence) {
                            if (geofence.activatedAt != null) {
                                "Active: "
                            } else {
                                "Last modified: "
                            } +
                                    if (geofence.activatedAt != null) {
                                        DateFormat.format(
                                            "EEE MMM dd, hh:mm a",
                                            geofence.activatedAt
                                        ).toString()
                                    } else {
                                        DateFormat.format(
                                            "EEE MMM dd, hh:mm a",
                                            geofence.modifiedAt
                                        ).toString()
                                    }
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (MaterialTheme.typography.bodySmall.fontSize.value - 1).sp),
                    )
                }
                IconButton(
                    onClick = { onDeleted(geofence.id) },
                    modifier = Modifier
                        .align(Alignment.Top)
                        .weight(1f),
                    colors = IconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete geofence",
                    )
                }
                Switch(
                    checked = geofence.activatedAt != null, onCheckedChange = {
                        onActivatedChanged(it, geofence.id)
                    },
                    modifier = Modifier
                        .align(Alignment.Top)
                        .weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ViewGeofenceListItemPreview() {
    ViewGeofenceListItem(
        geofence = Geofence(
            id = 1,
            name = "testing two ",
            radius = 10.0,
            latLong = LatLong(
                latitude = 10.0,
                longitude = 10.0
            ),
            activatedAt = Date(System.currentTimeMillis()),
            modifiedAt = Date(System.currentTimeMillis()),
            createdAt = Date(System.currentTimeMillis()),
            bitmapFilePath = "",
        ),
        modifier = Modifier.width(300.dp),
        onActivatedChanged = { _, _ -> },
        onDeleted = {},
        onGeofenceClicked = {}
    )
}