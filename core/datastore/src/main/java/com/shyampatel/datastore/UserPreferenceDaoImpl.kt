package com.shyampatel.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    private val FCM_TOKEN = stringPreferencesKey("fcm_token")
    private val FID = stringPreferencesKey("fid")

    override fun getUserAccessToken(): Flow<String?> {
        return preferencesDataStore.data
            .map { preferences -> preferences[UA_TOKEN] }
    }

    override suspend fun saveUserAccessToken(token: String): String {
        return withContext(ioDispatcher) {
            return@withContext preferencesDataStore.edit { preferences ->
                preferences[UA_TOKEN] = token
            }[UA_TOKEN]!!
        }
    }

    override suspend fun clearAccessToken() {
        withContext(ioDispatcher){
            preferencesDataStore.edit { preferences ->
                preferences.remove(UA_TOKEN)
            }
        }
    }

    override suspend fun saveAuthenticatedOwner(owner: RepoOwner): RepoOwner {
        return withContext(ioDispatcher) {
            val preferences = preferencesDataStore.edit { preferences ->
                preferences[OWNER_ID] = owner.serverId
                preferences[OWNER_LOGIN] = owner.login
                preferences[OWNER_NAME] = owner.name?: ""
                preferences[OWNER_TYPE] = owner.type.toString()
                preferences[OWNER_COMPANY] = owner.company?: ""
                preferences[OWNER_AVATAR_URL] = owner.avatarUrl
                preferences[OWNER_HTML_URL] = owner.htmlUrl
            }
            return@withContext preferencesToRepoOwner(preferences)
        }
    }

    override fun getAuthenticatedRepoOwner(): Flow<RepoOwner?> {
       return preferencesDataStore.data.map {
           preferences ->
           val ownerId = preferences[OWNER_ID]
           if(ownerId.isNullOrEmpty())
               return@map null
           else {
               preferencesToRepoOwner(preferences)
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

    override suspend fun saveNotificationEnabled(enabled: Boolean): Boolean {
        return withContext(ioDispatcher) {
            return@withContext preferencesDataStore.edit { preferences ->
                preferences[NOTIFICATION_ENABLED] = enabled
            }[NOTIFICATION_ENABLED]!!
        }
    }

    override fun getNotificationEnabled(): Flow<Boolean?> {
        return preferencesDataStore.data
            .map { preferences -> preferences[NOTIFICATION_ENABLED] }
    }

    override suspend fun clearNotificationPreference() {
        withContext(ioDispatcher){
            preferencesDataStore.edit { preferences ->
                preferences.remove(NOTIFICATION_ENABLED)
            }
        }
    }

    override suspend fun saveFCMToken(token: String): String {
        return withContext(ioDispatcher) {
            return@withContext preferencesDataStore.edit { preferences ->
                preferences[FCM_TOKEN] = token
            }[FCM_TOKEN]!!
        }
    }

    override fun getFCMToken(): Flow<String?> {
        return preferencesDataStore.data
            .map { preferences -> preferences[FCM_TOKEN] }
    }

    override suspend fun clearFCMToken() {
        withContext(ioDispatcher){
            preferencesDataStore.edit { preferences ->
                preferences.remove(FCM_TOKEN)
            }
        }
    }

    override suspend fun saveFid(fid: String): String {
        return withContext(ioDispatcher) {
            return@withContext preferencesDataStore.edit { preferences ->
                preferences[FID] = fid
            }[FID]!!
        }
    }

    override fun getFid(): Flow<String?> {
        return preferencesDataStore.data
            .map { preferences -> preferences[FID] }
    }

    override suspend fun clearFid() {
        withContext(ioDispatcher) {
            preferencesDataStore.edit { preferences ->
                preferences.remove(FID)
            }
        }
    }

    private fun preferencesToRepoOwner(preferences: Preferences): RepoOwner {
        return RepoOwner(
            serverId = preferences[OWNER_ID]!!,
            login = preferences[OWNER_LOGIN]!!,
            name = if (preferences[OWNER_NAME].isNullOrEmpty()) null else preferences[OWNER_NAME],
            type =  enumValueOf<RepoOwnerType>(preferences[OWNER_TYPE]!!.uppercase(Locale.getDefault())),
            avatarUrl = preferences[OWNER_AVATAR_URL]!!,
            htmlUrl = preferences[OWNER_HTML_URL]!!,
            company = if (preferences[OWNER_COMPANY].isNullOrEmpty()) null else preferences[OWNER_COMPANY],)
    }
}