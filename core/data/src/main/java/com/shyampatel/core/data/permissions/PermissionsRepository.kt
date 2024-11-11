package com.shyampatel.core.data.permissions

import kotlinx.coroutines.flow.Flow

interface PermissionsRepository {
    suspend fun savePermissionsMap(permissions: Map<String, Boolean>)
    fun getPermissionsMap(): Flow<Result<Map<String, Boolean>>>
}