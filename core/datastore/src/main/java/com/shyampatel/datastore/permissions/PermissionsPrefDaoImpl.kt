package com.shyampatel.datastore.permissions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject


internal class PermissionsPrefDaoImpl(
    private val preferencesDataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher
) : PermissionsPrefDao {

    private val PERMISSIONS = stringPreferencesKey("permissions")

    override suspend fun saveMap(permissionsMap: Map<String, Boolean>) {
        withContext(ioDispatcher) {
            val jsonObject = JSONObject(permissionsMap)
            val jsonString = jsonObject.toString()
            preferencesDataStore.edit { preferences ->
                preferences.remove(PERMISSIONS)
                preferences[PERMISSIONS] = jsonString
            }
        }
    }

    override fun loadMap(): Flow<Map<String, Boolean>> {
        return preferencesDataStore.data.map { preferences ->
            val outputMap: MutableMap<String, Boolean> = HashMap()
            preferences[PERMISSIONS]?.let {
                val json = JSONObject(it)
                val keysItr = json.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    val value = json.getBoolean(key)
                    outputMap[key] = value
                }
                outputMap
            } ?: emptyMap()
        }
    }
}