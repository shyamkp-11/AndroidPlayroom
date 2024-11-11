package com.shyampatel.datastore.permissions

import kotlinx.coroutines.flow.Flow

interface PermissionsPrefDao {
    fun loadMap(): Flow<Map<String, Boolean>>
    suspend fun saveMap(permissionsMap: Map<String, Boolean>)
}