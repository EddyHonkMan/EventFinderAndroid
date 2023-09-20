package com.example.eventfinder

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

object FavoriteDataStoreSingleton {
    const val FAVORITE_DATASTORE_NAME = "favorite_datastore"
    private val key : Preferences.Key<String> = stringPreferencesKey(FAVORITE_DATASTORE_NAME)
    private lateinit var dataStore: DataStore<Preferences>
    private var favoriteEvents: MutableList<EventData> = mutableListOf()
    private var favoriteEventsIdSet: MutableSet<EventData> = mutableSetOf()
    private var isInitialized = false
    private lateinit var deferred: Deferred<Unit>
    fun init(dataStore: DataStore<Preferences>, scope: CoroutineScope) {
        this.dataStore = dataStore
        deferred = scope.async {
            load()
            isInitialized = true
        }
    }

    suspend fun getFavoriteEvents(): MutableList<EventData> {
        if (!isInitialized) {
            deferred.await()
        }
        return favoriteEvents
    }

    suspend fun addEvent(eventData: EventData) {
        if (!isInitialized) {
            deferred.await()
        }
        favoriteEvents += eventData
        favoriteEventsIdSet.add(eventData)
        save()
    }

    suspend fun removeEvent(eventData: EventData) {
        if (!isInitialized) {
            deferred.await()
        }
        val index = favoriteEvents.indexOf(eventData)
        if (index != -1) {
            val holder = favoriteEvents.removeAt(index)
            favoriteEventsIdSet.remove(holder)
            save()
        }
    }

    suspend fun hasEvent(eventData: EventData) : Boolean {
        if (!isInitialized) {
            deferred.await()
        }
        return favoriteEventsIdSet.contains(eventData)
    }

    suspend fun indexOf(eventData: EventData) : Int {
        if (!isInitialized) {
            deferred.await()
        }
        return favoriteEvents.indexOf(eventData)
    }

    private suspend fun load() {
        val flow = dataStore.data.map {preferences -> preferences[key] ?: "[]"}
        val serialized = flow.first()
        favoriteEvents = Json.decodeFromString(serialized)
        favoriteEvents.forEach { favoriteEventsIdSet.add(it) }
    }

    private suspend fun save() {
        val serializedFavoriteEvents:String = Json.encodeToString(favoriteEvents)
        dataStore.edit { preferences ->
            preferences[key] = serializedFavoriteEvents
        }
    }
}