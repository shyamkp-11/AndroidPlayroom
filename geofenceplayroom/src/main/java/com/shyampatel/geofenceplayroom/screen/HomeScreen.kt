package com.shyampatel.geofenceplayroom.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.shyampatel.geofenceplayroom.R
import com.shyampatel.ui.AndroidPlayroomTopAppBar
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import org.koin.androidx.compose.koinViewModel


@Composable
internal fun HomeScreenRoute(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel(),
) {
    HomeScreen(
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AndroidPlayroomTopAppBar(
                titleRes = R.string.app_name,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(top = 10.dp),
        ) {
            Surface(
                modifier = Modifier.padding(
                    top = 24.dp
                ),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                val singapore = LatLng(1.35, 103.87)
                val singaporeMarkerState = rememberMarkerState(position = singapore)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(singapore, 10f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(
                    ),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = singaporeMarkerState,
                        title = "Singapore",
                        snippet = "Marker in Singapore"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidPlayroomTheme {
        HomeScreen(
        )
    }
}