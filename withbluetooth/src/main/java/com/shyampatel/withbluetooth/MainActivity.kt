package com.shyampatel.withbluetooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import com.shyampatel.withbluetooth.navigation.HomeNavigation
import com.shyampatel.withbluetooth.navigation.WithBluetoothAppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberWithBluetoothAppState()
            AndroidPlayroomTheme {
                GeoFencePlayroomApp(appState, modifier = Modifier)
            }
        }
    }
}

@Composable
fun GeoFencePlayroomApp(appState: WithBluetoothAppState, modifier: Modifier) {
    AndroidPlayroomTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceDim,
            modifier = modifier.fillMaxSize(),
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            GeoFencePlayroomApp(appState, snackbarHostState)
        }
    }
}

@Composable
fun GeoFencePlayroomApp(
    appState: WithBluetoothAppState,
    snackbarHostState: SnackbarHostState,
) {
    WithBluetoothAppNavHost(
        appState = appState,
        navController = appState.navController,
        modifier = Modifier,
        startDestination = HomeNavigation::class,
        snackbarHostState = snackbarHostState,
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    GeoFencePlayroomApp(appState = rememberWithBluetoothAppState(), snackbarHostState = remember {
        SnackbarHostState()
    })
}