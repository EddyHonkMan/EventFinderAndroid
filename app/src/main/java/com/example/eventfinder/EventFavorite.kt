package com.example.eventfinder

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier

data class EventFavorite(val eventId: String, val operation: FavoriteOperations, val srcId: SrcId, val originalIndexInFavoriteList: Int = -1)
