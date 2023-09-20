package com.example.eventfinder

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ProgressBar
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class KeywordAutoCompleteAdapter(
    context: Context,
    resource: Int,
    private val coroutineScope: CoroutineScope,
    private val progressBar: ProgressBar
) : ArrayAdapter<String>(context, resource) {
    private var suggestions = mutableListOf<String>()
    private val debounceTime = TimeUnit.MILLISECONDS.convert(300, TimeUnit.MILLISECONDS)
    private var debounceJob: Job? = null

    fun onTextChanged(text: CharSequence?) {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(debounceTime)
            if (text != null && text.isNotEmpty()) {
                fetchSuggestions(text.toString())
            }
            else clear()
        }
    }

    private fun fetchSuggestions(keyword: String) {
        Log.d("Fetch suggestion for keyword", keyword)

        progressBar.visibility = View.VISIBLE
        val url = "https://rzhu2918.wl.r.appspot.com/autocomplete?keyword=$keyword"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                clear()
                val attractions = it.optJSONObject("_embedded")?.optJSONArray("attractions")
                if (attractions != null && attractions.length() != 0) {
                    suggestions = mutableListOf<String>()
                    for(i in 0 until attractions.length()) {
                        val suggestion = attractions.getJSONObject(i)?.optString("name")
                        if (suggestion != null && suggestion.isNotEmpty()) {
//                            Log.d("suggestion", suggestion)
                            suggestions.add(suggestion)
                        }
                    }
                    addAll(suggestions)
                }
                progressBar.visibility = View.INVISIBLE
            },
            {
                //error(it)
            }
        )

        request.tag = "autocomplete"
        VolleySingleton.cancelAll("autocomplete")
        VolleySingleton.pushQ(request)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            //do not filter
            override fun performFiltering(constraint: CharSequence?): FilterResults? {
                return null
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
        }
    }
}
