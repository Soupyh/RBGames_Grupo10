package com.example.rbgames_grupo1.data.local.storage


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//extension para manipular el DataStore
val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences (private val context: Context){
    //declarar las key de guardado de datos de mi DataStore
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")

    //funcion para modificar el valor de la variable Data Store
    suspend fun setLoggedIn(value: Boolean){
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = value
        }
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            prefs[isLoggedInKey] ?: false
        }

}

