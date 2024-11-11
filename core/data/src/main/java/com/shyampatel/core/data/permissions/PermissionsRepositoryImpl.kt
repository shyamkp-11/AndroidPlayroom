package com.shyampatel.core.data.permissions

import com.shyampatel.datastore.permissions.PermissionsPrefDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class PermissionsRepositoryImpl(
    private val dataStore: PermissionsPrefDao,
    private val ioDispatcher: CoroutineDispatcher
): PermissionsRepository {
    override suspend fun savePermissionsMap(permissions: Map<String, Boolean>) {
        dataStore.saveMap(permissions)
    }

    override fun getPermissionsMap(): Flow<Result<Map<String, Boolean>>> {
        return dataStore.loadMap().map {
            Result.success(it)
        }.flowOn(ioDispatcher)
    }
}