package com.example.eventfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.eventfinder.databinding.FragmentArtistBinding

class ArtistFragment(private val artistsInfo: MutableList<MusicRelatedArtistData?>) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentArtistBinding.inflate(inflater, container, false)

        val list = artistsInfo.filterNotNull().toMutableList()
        if (list.isEmpty()) {
            binding.artistList.visibility = View.GONE
            binding.noData.visibility = View.VISIBLE
            return binding.root
        }
        val artistsRecyclerViewAdapter = ArtistsRecyclerViewAdapter(list, Utilities.getActivityLifeCycleScope(context)!!)
        binding.artistList.adapter = artistsRecyclerViewAdapter

        return binding.root
    }
}