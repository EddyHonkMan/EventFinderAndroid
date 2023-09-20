package com.example.eventfinder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {
//    private val args: ResultFragmentArgs by navArgs()
    private lateinit var goBackBtn: ConstraintLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResult: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        recyclerView = view.findViewById(R.id.result_list)
        progressBar = view.findViewById(R.id.result_progress)
        goBackBtn = view.findViewById(R.id.goBack_btn)
        noResult = view.findViewById(R.id.no_result)

        goBackBtn.setOnClickListener() {
            findNavController().navigateUp()
        }

//        val events = List(10){EventData()}
//        val recyclerViewAdapter = ResultListAdapter(events, lifecycleScope)
//        recyclerView.adapter = recyclerViewAdapter
//        recyclerViewAdapter.listenToFavoriteChange()
//
//        progressBar.visibility = View.GONE

        recyclerView.visibility = View.GONE
        doSearch()
        return view
    }

    private fun doSearch() {
        val viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        var url = "https://rzhu2918.wl.r.appspot.com/events_search?keyword=${viewModel.keyword}" +
                "&distance=${viewModel.distance}" + "&segmentId=${viewModel.category}"
        var ipGeo: String = ""
        if (viewModel.autoDetect) {
            lifecycleScope.launch {
                ipGeo = viewModel.autoDetectFlow!!.filter { it.isNotEmpty() }.first()
            }
            if (ipGeo == "fail") {
                Utilities.showSnackbar(noResult, "Failed to retrieve ipGeo info, please retry search")
                return
            }
            url += "&location=$ipGeo&autodetect=on"
        } else {
            url += "&location=${viewModel.location}"
        }

        url.replace(Regex("\\s+"), "+")
        Log.d("event search", url)


        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val events = EventData.listFromJson(it)
                if (events.isNotEmpty()) {
                    val recyclerViewAdapter =
                        ResultListAdapter(events, Utilities.getActivityLifeCycleScope(context)!!)
                    recyclerView.adapter = recyclerViewAdapter
                    recyclerView.visibility = View.VISIBLE
                    recyclerViewAdapter.listenToFavoriteChange()
                } else {
                    noResult.visibility = View.VISIBLE
                }
                progressBar.visibility = View.GONE
            },
            {
                //error(it)
            }
        )

        request.tag = "Search"
        VolleySingleton.cancelAll("Search")
        VolleySingleton.pushQ(request)
    }
}

