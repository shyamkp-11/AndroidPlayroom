package com.shyampatel.withbluetooth

import android.content.Context
import com.shyampatel.ui.permissions.PermissionsViewModel
import com.shyampatel.withbluetooth.screens.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getAppModule(appContext: Context) = module {

    viewModel {
        PermissionsViewModel(
            permissionsRepository = get()
        )
    }
    viewModel {
        HomeViewModel()
    }
}