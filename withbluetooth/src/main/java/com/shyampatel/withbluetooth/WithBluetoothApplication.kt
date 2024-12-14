package com.shyampatel.withbluetooth

import android.app.Application
import com.shyampatel.core.data.geofence.getDataModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup.onKoinStartup
import org.koin.core.qualifier.named
import org.koin.dsl.module

class WithBluetoothApplication : Application() {

    init {
        val coroutineScope = CoroutineScope(SupervisorJob())
        onKoinStartup {
            androidContext(this@WithBluetoothApplication)
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
                    applicationContext = applicationContext,
                    defaultDispatcher = Dispatchers.Default,
                    ioDispatcher = Dispatchers.IO
                ),
                getAppModule(
                    appContext = applicationContext
                ),
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}