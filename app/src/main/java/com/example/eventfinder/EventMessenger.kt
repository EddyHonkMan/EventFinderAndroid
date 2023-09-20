package com.example.eventfinder

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object EventMessenger {
    val flow = MutableSharedFlow<EventFavorite>()
}