package com.shyampatel.ui.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shyampatel.core.data.permissions.PermissionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val permissionsRepository: PermissionsRepository
): ViewModel() {

    val permissionsMap: StateFlow<Map<String, Boolean>?> = permissionsRepository.getPermissionsMap().map {
        it.getOrDefault(emptyMap())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    fun savePermissionsMap(permissions: Map<String, Boolean>) {
        viewModelScope.launch {
            permissionsRepository.savePermissionsMap(permissions)
        }
    }
}
