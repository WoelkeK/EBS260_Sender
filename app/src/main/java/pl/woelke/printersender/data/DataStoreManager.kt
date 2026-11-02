package pl.woelke.printersender.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Initialize DataStore at application context
 */
val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(context: Context) {
    private val dataStore = context.dataStore

    private val SAVED_IP_ADDRESSES_KEY = stringSetPreferencesKey("saved_ip_addresses")
    private val LAST_IP_KEY = stringPreferencesKey("last_ip")
    private val LAST_PORT_KEY = stringPreferencesKey("last_port")
    private val IS_ACTIVATED_KEY = booleanPreferencesKey("is_activated")


    val savedIpAddresses: Flow<Set<String>> = dataStore.data
        .map { preferences -> preferences[SAVED_IP_ADDRESSES_KEY] ?: emptySet() }

    suspend fun saveIpAddresses(ipAddresses: Set<String>) {
        dataStore.edit { preferences ->
            preferences[SAVED_IP_ADDRESSES_KEY] = ipAddresses
        }
    }

    val lastIpAddress: Flow<String?> = dataStore.data
        .map { preferences -> preferences[LAST_IP_KEY] }

    val lastPort: Flow<String?> = dataStore.data
        .map { preferences -> preferences[LAST_PORT_KEY] }

    suspend fun saveLastIpAddress(ipAddress: String) {
        dataStore.edit { preferences ->
            preferences[LAST_IP_KEY] = ipAddress
        }
    }

    suspend fun saveLastPort(port: String) {
        dataStore.edit { preferences ->
            preferences[LAST_PORT_KEY] = port
        }
    }

    /**
     *  App Activation status
     */
    val isActivated: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_ACTIVATED_KEY] ?: false }

    /**
     *  Store activation status
     */
    suspend fun setActivated(isActivated: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ACTIVATED_KEY] = isActivated
        }
    }
}
