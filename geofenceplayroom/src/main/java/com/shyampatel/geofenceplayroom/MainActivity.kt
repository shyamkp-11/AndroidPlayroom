package com.shyampatel.geofenceplayroom

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.compose.ComposeNavigator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shyampatel.geofenceplayroom.navigation.FencesTriggeredNavigation
import com.shyampatel.geofenceplayroom.navigation.GeoFencePlayroomNavHost
import com.shyampatel.geofenceplayroom.navigation.HomeNavigation
import com.shyampatel.geofenceplayroom.navigation.TopLevelDestination
import com.shyampatel.geofenceplayroom.navigation.permissions.PermissionNotificationNavigation
import com.shyampatel.geofenceplayroom.navigation.permissions.PermissionsBackgroundLocationNavigation
import com.shyampatel.geofenceplayroom.navigation.permissions.PermissionsForegroundLocationNavigation
import com.shyampatel.geofenceplayroom.navigation.permissions.navigateForegroundLocationPermissions
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import com.shyampatel.ui.util.dp
import kotlinx.coroutines.launch

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
            val snackbarHostState = remember { SnackbarHostState() }
            GeoFencePlayroomApp(appState, snackbarHostState)
        }
    }
}

@Composable
fun GeoFencePlayroomApp(
    appState: GeoFencePlayroomAppState,
    snackbarHostState: SnackbarHostState,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val gesturesEnabled = !(appState.currentDestination?.route==PermissionNotificationNavigation::class.qualifiedName ||
            appState.currentDestination?.route==PermissionsBackgroundLocationNavigation::class.qualifiedName ||
            appState.currentDestination?.route==PermissionsForegroundLocationNavigation::class.qualifiedName) &&
            (drawerState.isOpen || (appState.currentTopLevelDestination != TopLevelDestination.HOME))
    ModalNavigationDrawer(
        gesturesEnabled = gesturesEnabled,
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(appState = appState) {
                appState.coroutineScope.launch {
                    drawerState.close()
                }
                appState.navigateToTopLevelDestination(it)
            }
        }
    ) {
        NavHost(appState, drawerState, snackbarHostState)
    }
}

@Composable
private fun DrawerContent(
    appState: GeoFencePlayroomAppState,
    onNavItemClick: (topLevelDestination: TopLevelDestination) -> Unit,
) {
    val selectedTopLevelDestination = appState.currentTopLevelDestination

    ModalDrawerSheet(
        modifier = Modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp))

            appState.topLevelDestinations.forEach {
                NavigationDrawerItem(
                    label = { Text(text = stringResource(it.titleTextId)) },
                    icon = { Icon(imageVector = it.unselectedIcon, contentDescription = null) },
                    selected = selectedTopLevelDestination == it,
                    onClick = { onNavItemClick.invoke(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NavHost(
    appState: GeoFencePlayroomAppState,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val permissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
        GeoFencePlayroomNavHost(
            appState = appState,
            navController = appState.navController,
            modifier = Modifier,
            startDestination = HomeNavigation::class,
            drawerState = drawerState,
            snackbarHostState = snackbarHostState,
        )
        if (!permissionState.allPermissionsGranted) {
            LaunchedEffect(key1 = permissionState) {
                appState.navController.navigateForegroundLocationPermissions(
                    NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(appState.navController.graph.startDestinationId, true)
                        .build()
                )
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidPlayroomTheme {
    }
}