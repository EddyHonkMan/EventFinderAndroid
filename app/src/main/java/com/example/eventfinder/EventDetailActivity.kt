package com.example.eventfinder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.eventfinder.databinding.ActivityEventDetailBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EventDetailActivity: AppCompatActivity() {
    private lateinit var event: EventData
    private val barrierFlow = MutableSharedFlow<Unit>()
    private var eventDetail: EventData? = null
    private lateinit var artistsInfo: MutableList<MusicRelatedArtistData?>
    private var venueDetail: VenueData? = null
    private var barrierSize = -1
    private lateinit var binding: ActivityEventDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        event = Json.decodeFromString(intent.getStringExtra("event")!!)
        doSearch(event.eventId)

        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackIcon()
        setupEventNameTextView()
        setupTwitterShare()
        setupFacebookShare()
        setupLikeButton()
    }

    private fun setupEventNameTextView() {
        binding.eventName.text = event.eventName
        binding.eventName.isSelected = true
    }

    private fun setupBackIcon() {
        binding.backIcon.setOnClickListener {
            this.finish()
        }
    }

    private fun getEventDetail(eventId: String) {
        val url = "https://rzhu2918.wl.r.appspot.com/event_details?id=$eventId".replace(Regex("\\s+"), "+")
        Log.d("get event detail", url)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                eventDetail = EventData.fromJson(it)
                eventDetail?.let {
                    //set barrier size, +1 for venue, getArtist should hit the barrier on whichever volley request result!
                    barrierSize = it.musicRelatedArtists.size + 1
                    lifecycleScope.launch {
                        var count = 0
                        barrierFlow.take(barrierSize).collect{
                            Log.d("barrier hit count",(++count).toString())
                        }
                        Log.d("barrier broken", "")

                        //get rid of the artistInfo we failed to populate
                        artistsInfo.filterNotNull()

                        setupTabLayoutAndViewPager()
                        binding.loadingPage.visibility = View.GONE
                    }
                    getArtistsInfo(it.musicRelatedArtists)
                    getVenueDetail(it.venueName)
                } ?: Utilities.showSnackbar(binding.root, "Event detail unavailable")
            },
            {
                //error(it)
            }
        )

        request.tag = "detail"
        VolleySingleton.cancelAll("detail")
        VolleySingleton.pushQ(request)
    }

    private fun getArtistInfo(artistName: String, store: MutableList<MusicRelatedArtistData?>, index: Int) {
        val url = "https://rzhu2918.wl.r.appspot.com/artists?keyword=$artistName".replace(Regex("\\s+"), "+")
        Log.d("get artist $artistName detail", url)
        val albumsFlow = MutableSharedFlow<Unit>()

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                store[index] = MusicRelatedArtistData.basicDataFromJson(it)
                store[index] ?.let {
                    lifecycleScope.launch{
                        albumsFlow.first()
                        barrierFlow.emit(Unit)
                    }
                    getAlbumsInfo(it.artistId, albumsFlow, it, artistName)
                } ?: lifecycleScope.launch{ barrierFlow.emit(Unit) }
            },
            {
                lifecycleScope.launch{
                    barrierFlow.emit(Unit)
                }
                //error(it)
            }
        )

//        request.tag = "artists"
//        VolleySingleton.cancelAll("artists")
        VolleySingleton.pushQ(request)
    }

    private fun getAlbumsInfo(
        artistId: String,
        albumsFlow: MutableSharedFlow<Unit>,
        musicRelatedArtistData: MusicRelatedArtistData,
        artistName: String = ""
    ) {
        val url = "https://rzhu2918.wl.r.appspot.com/albums?keyword=$artistId".replace(Regex("\\s+"), "+")
        Log.d("get $artistName albums", url)
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            {
                MusicRelatedArtistData.populateAlbumsDataFromJson(it, musicRelatedArtistData)
                lifecycleScope.launch{
                    albumsFlow.emit(Unit)
                }
            },
            {
                lifecycleScope.launch{
                    albumsFlow.emit(Unit)
                }
                //error(it)
            }
        )

//        request.tag = "artists"
//        VolleySingleton.cancelAll("artists")
        VolleySingleton.pushQ(request)
    }

    private fun getArtistsInfo(musicRelatedArtists: List<String>) {
        //prepare store place for fetched data
        artistsInfo = MutableList(musicRelatedArtists.size){ null }

        //fetch
        for(i in musicRelatedArtists.indices){
            getArtistInfo(musicRelatedArtists[i], artistsInfo, i)
        }
    }

    private fun getVenueDetail(venueName: String) {
        val url = "https://rzhu2918.wl.r.appspot.com/venue_details?keyword=$venueName".replace(Regex("\\s+"), "+")
        Log.d("get venue detail", url)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                venueDetail = VenueData.fromJson(it)
                lifecycleScope.launch{
                    barrierFlow.emit(Unit)
                }
            },
            {
                //error(it)
            }
        )

        request.tag = "venue"
        VolleySingleton.cancelAll("venue")
        VolleySingleton.pushQ(request)
    }

    private fun doSearch(url: String) {
        getEventDetail(url)
    }

    private fun setupLikeButton() {
        lifecycleScope.launch {
            if (FavoriteDataStoreSingleton.hasEvent(event)) {
                binding.like.setImageResource(R.drawable.heart_filled)
            } else {
                binding.like.setImageResource(R.drawable.heart_outline)
            }
        }
        binding.like.setOnClickListener{
            lifecycleScope.launch {
                if (FavoriteDataStoreSingleton.hasEvent(event)) {
                    binding.like.setImageResource(R.drawable.heart_outline)
                    val index = FavoriteDataStoreSingleton.indexOf(event)
//                        Log.d("remove favorite from result list", "index$index")
                    FavoriteDataStoreSingleton.removeEvent(event)
                    EventMessenger.flow.emit(
                        EventFavorite(
                            eventId = event.eventId,
                            operation = FavoriteOperations.REMOVE,
                            SrcId.DETAIL,
                            index
                        )
                    )
                    Utilities.showSnackbar(it, "${event.eventName} removed from favorites")
                } else {
                    binding.like.setImageResource(R.drawable.heart_filled)
                    FavoriteDataStoreSingleton.addEvent(event)
                    EventMessenger.flow.emit(
                        EventFavorite(
                            eventId = event.eventId,
                            operation = FavoriteOperations.ADD,
                            SrcId.DETAIL
                        )
                    )
                    Utilities.showSnackbar(it, "${event.eventName} added to favorites")
                }
            }
        }
    }

    private fun setupFacebookShare() {
        binding.facebook.setOnClickListener{
            val url = "https://www.facebook.com/sharer/sharer.php?u=${event.ticketURL}&amp;src=sdkpreparse"
            Log.d("facebook share url", url)

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun setupTwitterShare() {
        binding.twitter.setOnClickListener{
            val url = "https://twitter.com/share?text=Check out ${if (event.eventName.isEmpty()) "" else (event.eventName + " ")}on Ticketmaster!&url=${event.ticketURL}"
            Log.d("twitter share url", url)

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun setupTabLayoutAndViewPager() {
        binding.viewPager.adapter = EventDetailViewPagerAdapter(this, eventDetail, artistsInfo, venueDetail)
        binding.viewPager.offscreenPageLimit = 2

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> {
                    tab.icon = AppCompatResources.getDrawable(this, R.drawable.info_icon)
                    tab.text = "DETAILS"
                }
                1 -> {
                    tab.icon = AppCompatResources.getDrawable(this, R.drawable.artist_icon)
                    tab.text = "ARTIST(S)"
                }
                2 -> {
                    tab.icon = AppCompatResources.getDrawable(this, R.drawable.venue_icon)
                    tab.text = "VENUE"
                }
            }
        }.attach()
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                val index = tab!!.position
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//            }
//
//        })
    }
}