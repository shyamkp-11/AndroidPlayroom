package com.shyampatel.geofenceplayroom.screen

import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {


    sealed interface HomeState {
        data object Error : HomeState
        data object Loading : HomeState
    }
}
