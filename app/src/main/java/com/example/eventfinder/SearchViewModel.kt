package com.example.eventfinder

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModel : ViewModel() {
    var keyword: String = ""
    var distance: String = ""
    var category: String = ""
    var location: String = ""
    var autoDetect = false
    var autoDetectFlow: MutableStateFlow<String>? = null
}
