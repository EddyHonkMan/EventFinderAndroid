package com.example.eventfinder

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SearchFormFragment : Fragment() {
    private lateinit var locationInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var switchCompat: SwitchCompat
    private lateinit var searchButton: Button
    private lateinit var clearButton: Button
    private lateinit var distanceInput: EditText
    private var geoInfoStateFlow: MutableStateFlow<String>? = null

    private lateinit var geoInfo: String

    private val segmentIDs = mapOf(
        "Music" to "KZFzniwnSyZfZ7v7nJ",
        "Sports" to "KZFzniwnSyZfZ7v7nE",
        "Arts & Theatre" to "KZFzniwnSyZfZ7v7na",
        "Film" to "KZFzniwnSyZfZ7v7nn",
        "Miscellaneous" to "KZFzniwnSyZfZ7v7n1",
        "All" to ""
    )
    private lateinit var viewModelProvider: ViewModelProvider

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_form, container, false)

        locationInput = view.findViewById(R.id.location)
        categorySpinner = view.findViewById(R.id.category_spinner)
        progressBar = view.findViewById(R.id.autocomplete_progress)
        autoCompleteTextView = view.findViewById(R.id.keyword_autocomplete)
        switchCompat = view.findViewById(R.id.auto_detect_switch)
        searchButton = view.findViewById(R.id.search_btn)
        clearButton = view.findViewById(R.id.clear_btn)
        distanceInput = view.findViewById(R.id.distance)

        //category spinner
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(requireContext(),
            R.array.category_entries, R.layout.spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        //autocomplete
        autoCompleteTextView.threshold = 1
        val keywordAutoCompleteAdapter = KeywordAutoCompleteAdapter(requireContext(),
            R.layout.autocomplete_dropdown_item_1line, lifecycleScope, progressBar)
        autoCompleteTextView.setAdapter(keywordAutoCompleteAdapter)
        autoCompleteTextView.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("onTextChanged", "called!")
                if (autoCompleteTextView.isPerformingCompletion) return
                keywordAutoCompleteAdapter.onTextChanged(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //switch
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                locationInput.visibility = View.GONE
                getGeoInfo()
            }
            else locationInput.visibility = View.VISIBLE
        }

        viewModelProvider = ViewModelProvider(requireActivity())
        //search_btn
        searchButton.setOnClickListener {
            if (reportValidity()) {
                triggerFragmentNavigation()
            }
        }


        clearButton.setOnClickListener{
            autoCompleteTextView.setText("")
            distanceInput.setText("10")
            categorySpinner.setSelection(0)
            switchCompat.isChecked = false
            locationInput.setText("")
        }

        return view
    }

    private fun getGeoInfo() {
        //reset state flow
        geoInfoStateFlow = MutableStateFlow("")

        val url = "https://ipinfo.io/json?token=a065591e973c9f"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val loc = it.optString("loc")
                if (loc.isNotEmpty()) {
                    geoInfo = loc.toString()
                    Log.d("geoInfo", geoInfo)
                    lifecycleScope.launch{
                        geoInfoStateFlow!!.emit(geoInfo)
                    }
                }
                else {
                    Log.d("geoInfo empty", "")
                    lifecycleScope.launch{
                        geoInfoStateFlow!!.emit("fail")
                    }
                }
            },
            {
                Log.d("get geoInfo failed", "")
                lifecycleScope.launch{
                    geoInfoStateFlow!!.emit("fail")
                }
                //error(it)
            }
        )

        request.tag = "ipGeo"
        VolleySingleton.cancelAll("ipGeo")
        VolleySingleton.pushQ(request)
    }

    private fun reportValidity(): Boolean{
        if (autoCompleteTextView.text.isEmpty() || distanceInput.text.isEmpty() || (!switchCompat.isChecked && locationInput.text.isEmpty())) {
            Utilities.showSnackbar(searchButton, "Please fill out all required fields end with *")
            return false
        }
        val distanceString = distanceInput.text.toString()
        val regex = Regex("[^0-9]")
        if (regex.containsMatchIn(distanceString) || distanceString.toInt() == 0) {
            Utilities.showSnackbar(searchButton, "Please fill out valid distance, should be positive integer")
            return false
        }
        return true
    }

    private fun triggerFragmentNavigation() {
        val viewModel = viewModelProvider[SearchViewModel::class.java]
        viewModel.autoDetect = switchCompat.isChecked
        viewModel.keyword = autoCompleteTextView.text.toString()
        viewModel.distance = distanceInput.text.toString()
        viewModel.category = segmentIDs[categorySpinner.selectedItem.toString()]!!
        viewModel.location = locationInput.text.toString()
        viewModel.autoDetectFlow = geoInfoStateFlow

        findNavController().navigate(SearchFormFragmentDirections.actionSearchFormFragmentToResultFragment())
    }
}
