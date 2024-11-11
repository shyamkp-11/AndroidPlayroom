package com.shyampatel.geofenceplayroom.screen.fencetriggered

import android.graphics.BitmapFactory
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shyampatel.core.common.geofence.Geofence
import com.shyampatel.core.common.geofence.GeofenceEvent
import com.shyampatel.core.common.geofence.GeofenceLog
import com.shyampatel.geofenceplayroom.R
import com.shyampatel.ui.AndroidPlayroomLoadingIndicator
import com.shyampatel.ui.AndroidPlayroomTopAppBar
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun FenceTriggeredScreenRoute(
    id: Long,
    topBarBackIconClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<FenceTriggeredViewModel>(
        parameters = { parametersOf(id) }
    )
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val logs by viewModel.getFenceLogs.collectAsStateWithLifecycle(null)
    val geofence by viewModel.getGeofence.collectAsStateWithLifecycle(null)
    FenceTriggeredScreen(
        backIconClick = topBarBackIconClick,
        loading = loading,
        geofence = geofence,
        logs = logs,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FenceTriggeredScreen(
    backIconClick: () -> Unit,
    loading: Boolean,
    geofence: Geofence?,
    logs: List<GeofenceLog>?,
    modifier: Modifier = Modifier,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    Scaffold(
        topBar = {
            AndroidPlayroomTopAppBar(
                backIconClick = backIconClick,
                titleRes = R.string.view_geofences_screen_top_bar_title,
                topAppBarState = topAppBarState
            )
        },
        modifier = modifier,
    )
    { innerPadding ->
        val contentModifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        LazyColumn(modifier = contentModifier, contentPadding = innerPadding) {
            geofence?.let { geofence ->
                item(
                    key = geofence.modifiedAt
                ) {
                    TitleComposable(geofence = geofence, modifier = Modifier)
                }
            }
            logs?.let { logs ->
                items(
                    items = logs,
                    key = {
                        it.timeStamp
                    }
                ) { task ->
                    val text = remember {
                        DateFormat.format(
                            "EEE MMM dd, hh:mm a",
                            task.timeStamp
                        ).toString()
                    }
                    FenceTriggeredScreenRow(
                        text = text,
                        imageVector = when (task.geofenceEvent) {
                            GeofenceEvent.TRANSITION_EXIT -> Icons.Default.Output
                            GeofenceEvent.TRANSITION_ENTER -> Icons.AutoMirrored.Default.Input
                            GeofenceEvent.TRANSITION_DWELL -> Icons.Default.PinDrop
                        },
                        contentDescription = when (task.geofenceEvent) {
                            GeofenceEvent.TRANSITION_EXIT -> "Geofence exit transition"
                            GeofenceEvent.TRANSITION_ENTER -> "Geofence enter transition"
                            GeofenceEvent.TRANSITION_DWELL -> "Geofence dwell transition"
                        },
                        iconTint = Color.White,
                        iconBackgroundColor = when (task.geofenceEvent) {
                            GeofenceEvent.TRANSITION_EXIT -> Color.Red
                            GeofenceEvent.TRANSITION_ENTER -> Color.Green
                            GeofenceEvent.TRANSITION_DWELL -> Color.Blue
                        },
                        onListItemClick = {},
                        modifier = Modifier,
                    )
                }
            }
        }
        if (loading) {
            AndroidPlayroomLoadingIndicator(modifier = modifier.padding(innerPadding))
        }
    }
}

@Composable
fun TitleComposable(
    geofence: Geofence,
    modifier: Modifier) {
    val image = remember {
        if (!geofence.bitmapFilePath.isNullOrEmpty()) {
            BitmapFactory.decodeFile(geofence.bitmapFilePath).asImageBitmap()
        } else {
            null
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp)
    ) {
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
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = geofence.name,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
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
            style = MaterialTheme.typography.bodySmall,
        )
    }
}


@Composable
fun FenceTriggeredSpacer(modifier: Modifier) {
    Spacer(
        modifier = modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
fun FenceTriggeredScreenRow(
    modifier: Modifier,
    text: String,
    imageVector: ImageVector, contentDescription: String,
    bottomSpacer: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconBackgroundColor: Color = Color.Transparent,
    onListItemClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .clickable {
                onListItemClick()
            },
    ) {
        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .background(
                    color = iconBackgroundColor,
                    shape = RoundedCornerShape(5.dp)
                )
                .align(Alignment.CenterVertically)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )
            }
            if (bottomSpacer) {
                FenceTriggeredSpacer(modifier)
            }
        }
    }
}