package com.shyampatel.geofenceplayroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.shyampatel.geofenceplayroom.navigation.GeoFencePlayroomNavHost
import com.shyampatel.ui.theme.AndroidPlayroomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberGeoFencePlayroomAppState()
            AndroidPlayroomTheme {
                GeoFencePlayroomApp(appState, modifier = Modifier)
            }
        }
    }
}

@Composable
fun GeoFencePlayroomApp(appState: GeoFencePlayroomAppState, modifier: Modifier) {
    AndroidPlayroomTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceDim,
            modifier = modifier.fillMaxSize(),
        ) {
            GeoFencePlayroomApp(appState)
        }
    }
}

@Composable
fun GeoFencePlayroomApp(appState: GeoFencePlayroomAppState) {
    GeoFencePlayroomNavHost(
        navController = appState.navController,
        modifier = Modifier,
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidPlayroomTheme {
    }
}