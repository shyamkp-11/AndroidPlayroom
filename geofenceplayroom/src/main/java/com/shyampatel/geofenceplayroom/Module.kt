package com.shyampatel.geofenceplayroom

import com.shyampatel.geofenceplayroom.screen.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun getAppModule() = module {
    viewModel {
        HomeViewModel()
    }
}