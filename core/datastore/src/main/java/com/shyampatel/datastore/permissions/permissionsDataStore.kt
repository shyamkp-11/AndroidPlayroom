package com.shyampatel.datastore.permissions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.permissionsPrefDataStore: DataStore<Preferences> by preferencesDataStore(name = "permissions_preferences")

