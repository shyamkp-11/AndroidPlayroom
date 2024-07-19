package com.shyampatel.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun getDataStoreModule(applicationContext: Context, ioDispatcher: CoroutineDispatcher) = module {
    single<DataStore<Preferences>>(named("preferencesDataStore")) { applicationContext.userPreferencesDataStore }
    factory<UserPreferencesDao> {
        UserPreferenceDaoImpl(preferencesDataStore = get(named("preferencesDataStore")), ioDispatcher = ioDispatcher)
    }
}