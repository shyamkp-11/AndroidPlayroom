package com.shyampatel.githubplayroom

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.shyampatel.core.data.getDataModule
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class GithubPlayroomApplication : Application(), ImageLoaderFactory {
    private lateinit var imageLoader: ImageLoader
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@GithubPlayroomApplication)
            allowOverride(false)
            modules(
                getDataModule(
                    baseUrl = "https://api.github.com",
                    applicationContext = applicationContext,
                    defaultDispatcher = Dispatchers.Default,
                    ioDispatcher = Dispatchers.IO
                ), getAppModule()
            )
        }
        imageLoader = koin.koin.get<ImageLoader>()
    }

    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
}