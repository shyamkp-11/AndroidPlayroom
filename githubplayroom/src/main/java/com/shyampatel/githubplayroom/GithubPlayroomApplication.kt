package com.shyampatel.githubplayroom

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.shyampatel.core.data.github.GithubRepository
import com.shyampatel.core.data.github.getDataModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.compose.getKoin
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module


class GithubPlayroomApplication : Application(), ImageLoaderFactory {
    private lateinit var imageLoader: ImageLoader

    init {
        val coroutineScope = CoroutineScope(SupervisorJob())
        KoinStartup.onKoinStartup {
            androidContext(this@GithubPlayroomApplication)
            allowOverride(false)
            modules(
                module {
                    single<CoroutineScope> {
                        coroutineScope
                    }
                    single<CoroutineDispatcher>(named("IO")) {
                        Dispatchers.IO
                    }
                },
                getDataModule(
                    baseUrl = "https://api.github.com",
                    graphqlBaseUrl = "https://api.github.com/graphql",
                    applicationContext = applicationContext,
                    defaultDispatcher = Dispatchers.Default,
                    ioDispatcher = Dispatchers.IO
                ), getAppModule()
            )
            imageLoader = koin.get<ImageLoader>()

        }
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseInstallations.getInstance().id.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                getKoin().apply {
                    get<CoroutineScope>().launch {
                        get<GithubRepository>().saveFid(token)
                    }
                }
            } else {
                // Log exception
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
}