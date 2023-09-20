package com.example.eventfinder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.eventfinder.databinding.FragmentVenueBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class VenueFragment(private val venueDetail: VenueData?) : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentVenueBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVenueBinding.inflate(inflater, container, false)

        if (venueDetail == null) {
            binding.validView.visibility = View.GONE
            binding.noData.visibility = View.VISIBLE
            return binding.root
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        venueDetail.venueName.takeIf { it.isNotEmpty() }
            ?.let { binding.venueName.text = it; binding.venueName.isSelected = true }
            ?: run { binding.venueNameEntry.visibility = View.GONE }
        venueDetail.address.takeIf { it.isNotEmpty() }
            ?.let { binding.address.text = it; binding.address.isSelected = true }
            ?: run { binding.addressEntry.visibility = View.GONE }
        venueDetail.cityAndState.takeIf { it.isNotEmpty() }
            ?.let { binding.cityState.text = it; binding.cityState.isSelected = true }
            ?: run { binding.cityStateEntry.visibility = View.GONE }
        venueDetail.contactInfo.takeIf { it.isNotEmpty() }
            ?.let { binding.contactInfo.text = it; binding.contactInfo.isSelected = true }
            ?: run { binding.contactInfoEntry.visibility = View.GONE }

        var visibleCount = 3
        venueDetail.openHours.takeIf { it.isNotEmpty() }?.let { binding.openHours.text = it }
            ?: run { binding.openHoursEntry.visibility = View.GONE; --visibleCount }
        venueDetail.generalRules.takeIf { it.isNotEmpty() }?.let { binding.generalRules.text = it }
            ?: run { binding.generalRulesEntry.visibility = View.GONE; --visibleCount }
        venueDetail.childRules.takeIf { it.isNotEmpty() }?.let { binding.childRules.text = it }
            ?: run { binding.childRulesEntry.visibility = View.GONE; --visibleCount }
        if (visibleCount == 0) {
            binding.venueRules.visibility = View.GONE
        }

        val rules = mutableListOf(binding.openHours, binding.generalRules, binding.childRules).filter { it.visibility == View.VISIBLE }
        rules.forEach{ view -> view.post {
                if (view.lineCount > 3) {
                    view.maxLines = 3
                    view.setOnClickListener {
                        textViewExpandOrCollapse(view)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onMapReady(gmap: GoogleMap) {
        if (venueDetail != null && venueDetail.lng.isNotEmpty() && venueDetail.lat.isNotEmpty()) {
            val markerPosition = LatLng(venueDetail.lat.toDouble(), venueDetail.lng.toDouble())
            gmap.addMarker(MarkerOptions().position(markerPosition).title(venueDetail.venueName))
            gmap.moveCamera(CameraUpdateFactory.newLatLng(markerPosition))
            gmap.setMinZoomPreference(16F)
        } else {
            Utilities.showSnackbar(binding.root, "Can't find venue lat,lng info, hide Google Map!")
        }
    }

    private fun textViewExpandOrCollapse(view: TextView) {
        when(view.maxLines) {
            3 -> view.maxLines = Integer.MAX_VALUE
            else -> view.maxLines = 3
        }
    }
}