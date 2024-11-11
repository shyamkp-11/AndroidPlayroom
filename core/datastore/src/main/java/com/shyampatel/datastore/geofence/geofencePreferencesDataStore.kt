package com.shyampatel.datastore.geofence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.geofencePreferencesDatastore: DataStore<Preferences> by preferencesDataStore(name = "settings")