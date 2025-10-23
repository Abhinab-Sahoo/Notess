package com.example.notess.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
private val IS_GRID_LAYOUT_KEY = booleanPreferencesKey("is_grid_layout")

class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveLayoutPreference(isGridLayout: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_GRID_LAYOUT_KEY] = isGridLayout
        }
    }

    val layoutPreference: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_GRID_LAYOUT_KEY] ?: true
        }
}