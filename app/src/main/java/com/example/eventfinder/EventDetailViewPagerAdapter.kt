package com.example.eventfinder

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter


class EventDetailViewPagerAdapter(
    fa: FragmentActivity,
    private val eventDetail: EventData?,
    private val artistsInfo: MutableList<MusicRelatedArtistData?>,
    private val venueDetail: VenueData?,
) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                DetailFragment(eventDetail)
            }
            1 -> {
                ArtistFragment(artistsInfo)
            }
            2 -> {
                VenueFragment(venueDetail)
            }
            else -> {
                throw IllegalAccessError("ViewPager index out of bound!")
            }
        }
    }
}