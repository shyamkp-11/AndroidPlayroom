package com.shyampatel.datastore.permissions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun getPermissionsDataStoreModule(applicationContext: Context, ioDispatcher: CoroutineDispatcher) = module {
    single<DataStore<Preferences>>(named("permissionsDataStore")) { applicationContext.permissionsPrefDataStore }
    factory<PermissionsPrefDao> {
        PermissionsPrefDaoImpl(preferencesDataStore = get(named("permissionsDataStore")), ioDispatcher = ioDispatcher)
    }
}