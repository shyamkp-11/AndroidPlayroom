package com.shyampatel.geofenceplayroom.repository

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

fun getLocationModule(context: Context) = module {
    factory<LocationRepository> {
        LocationRepository(
            sharedLocationManager = get()
        )
    }
    single {
        SharedLocationManager(
            context = context,
            externalScope = get()
        )
    }
}