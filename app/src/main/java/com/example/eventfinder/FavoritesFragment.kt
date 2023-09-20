package com.example.eventfinder

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var favoriteEvents: MutableList<EventData>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        recyclerView = view.findViewById(R.id.result_list)
        progressBar = view.findViewById(R.id.result_progress)
        textView = view.findViewById(R.id.no_favorite)

        lifecycleScope.launch{
//            Log.d("favorite coroutine", "")
            favoriteEvents = FavoriteDataStoreSingleton.getFavoriteEvents()
            val recyclerViewAdapter = FavoriteListAdapter(favoriteEvents, Utilities.getActivityLifeCycleScope(context)!!, recyclerView)
            recyclerView.adapter = recyclerViewAdapter
            recyclerViewAdapter.listenToFavoriteChange()

            if (favoriteEvents.isEmpty()) {
                recyclerView.visibility = View.GONE
            }

            progressBar.visibility = View.GONE
        }

        return view
    }
}