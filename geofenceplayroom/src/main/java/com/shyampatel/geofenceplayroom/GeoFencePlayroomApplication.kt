package com.shyampatel.geofenceplayroom

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GeoFencePlayroomApplication : Application() {
    private lateinit var imageLoader: ImageLoader
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@GeoFencePlayroomApplication)
            allowOverride(false)
            modules(
                getAppModule()
            )
        }
    }
}