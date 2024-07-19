package com.shyampatel.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * TODO Make a proto data store
 */
class UserPreferenceDaoImpl(private val preferencesDataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher): UserPreferencesDao {
    private val UA_TOKEN = stringPreferencesKey("ua_token")
    private val OWNER_ID = stringPreferencesKey("owner_id")
    private val OWNER_LOGIN = stringPreferencesKey("owner_login")
    private val OWNER_NAME = stringPreferencesKey("owner_name")
    private val OWNER_COMPANY = stringPreferencesKey("owner_company")
    private val OWNER_AVATAR_URL = stringPreferencesKey("owner_avatar_url")
    private val OWNER_HTML_URL = stringPreferencesKey("owner_html_url")
    private val OWNER_TYPE = stringPreferencesKey("owner_type")

    override fun getUserAccessToken(): Flow<String?> {
        return preferencesDataStore.data
            .map { preferences -> preferences[UA_TOKEN] }
    }

    override suspend fun saveUserAccessToken(token: String) {
        withContext(ioDispatcher) {
            preferencesDataStore.edit { preferences ->
                preferences[UA_TOKEN] = token
            }
        }
    }

    override suspend fun clearAccessToken() {
        withContext(ioDispatcher){
            preferencesDataStore.edit { preferences ->
                preferences.remove(UA_TOKEN)
            }
        }
    }

    override suspend fun saveAuthenticatedOwner(owner: RepoOwner) {
        withContext(ioDispatcher) {
            preferencesDataStore.edit { preferences ->
                preferences[OWNER_ID] = owner.id.toString()
                preferences[OWNER_LOGIN] = owner.login
                preferences[OWNER_NAME] = owner.name?: ""
                preferences[OWNER_TYPE] = owner.type.toString()
                preferences[OWNER_COMPANY] = owner.company?: ""
                preferences[OWNER_AVATAR_URL] = owner.avatarUrl
                preferences[OWNER_HTML_URL] = owner.htmlUrl
            }
        }
    }

    override fun getAuthenticatedRepoOwner(): Flow<RepoOwner?> {
       return preferencesDataStore.data.map {
           preferences ->
           val ownerId = preferences[OWNER_ID]
           if(ownerId.isNullOrEmpty() || ownerId.toLong() == 0L)
               return@map null
           else {
               RepoOwner(
                   id = ownerId.toLong(),
                   login = preferences[OWNER_LOGIN]!!,
                   name = if (preferences[OWNER_NAME].isNullOrEmpty()) null else preferences[OWNER_NAME],
                   type =  enumValueOf<RepoOwnerType>(preferences[OWNER_TYPE]!!.uppercase(Locale.getDefault())),
                   avatarUrl = preferences[OWNER_AVATAR_URL]!!,
                   htmlUrl = preferences[OWNER_HTML_URL]!!,
                   company = if (preferences[OWNER_COMPANY].isNullOrEmpty()) null else preferences[OWNER_COMPANY],
               )
           }
       }
    }

    override suspend fun clearAuthenticatedRepoOwner() {
        withContext(ioDispatcher){
            preferencesDataStore.edit { preferences ->
                preferences.remove(OWNER_ID)
                preferences.remove(OWNER_LOGIN)
                preferences.remove(OWNER_NAME)
                preferences.remove(OWNER_TYPE)
                preferences.remove(OWNER_COMPANY)
                preferences.remove(OWNER_AVATAR_URL)
                preferences.remove(OWNER_HTML_URL)
            }
        }
    }
}