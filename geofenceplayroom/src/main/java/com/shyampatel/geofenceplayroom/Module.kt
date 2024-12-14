package com.shyampatel.geofenceplayroom

import android.content.Context
import com.shyampatel.geofenceplayroom.screen.fencetriggered.FenceTriggeredViewModel
import com.shyampatel.geofenceplayroom.screen.home.HomeViewModel
import com.shyampatel.ui.permissions.PermissionsViewModel
import com.shyampatel.geofenceplayroom.screen.viewgeofences.ViewGeofencesViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getAppModule(appContext: Context) = module {
    single<NotificationManager> {
        NotificationManager(geofencesRepository = get(), context = appContext)
    }
    factory<GeofenceManager> {
        GeofenceManager(appContext)
    }
    viewModel {
        HomeViewModel(
            locationRepository = get(),
            geofenceRepository = get(),
            geofenceManager = get()
        )
    }
    viewModel {
        ViewGeofencesViewModel(
            notificationPermissionGranted = get(),
            geofenceManager = get(),
            geofenceRepository = get()
        )
    }
    viewModel {
        FenceTriggeredViewModel(
            id = get(),
            geofenceManager = get(),
            geofenceRepository = get()
        )
    }

    viewModel {
        PermissionsViewModel(
            permissionsRepository = get()
        )
    }
}