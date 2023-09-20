package com.example.eventfinder

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.eventfinder.databinding.FragmentDetailBinding
import com.squareup.picasso.Picasso

class DetailFragment(private val eventDetail: EventData?): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        if (eventDetail == null) {
            binding.validView.visibility = View.GONE
            binding.noData.visibility = View.VISIBLE
            return view
        }

        if (eventDetail.attractions.isNotEmpty()) {
            binding.artistTeamsName.text = eventDetail.attractions
            binding.artistTeamsEntry.visibility = View.VISIBLE
            binding.artistTeamsName.isSelected = true
        }
        if (eventDetail.venueName.isNotEmpty()) {
            binding.venueName.text = eventDetail.venueName
            binding.venueEntry.visibility = View.VISIBLE
            binding.venueName.isSelected = true
        }
        if (eventDetail.date.isNotEmpty()) {
            binding.date.text = Utilities.eventDetailDateFormatter.format(Utilities.outputDateFormatter.parse(eventDetail.date))
            binding.dateEntry.visibility = View.VISIBLE
            binding.date.isSelected = true
        }
        if (eventDetail.time.isNotEmpty()) {
            binding.time.text = eventDetail.time
            binding.timeEntry.visibility = View.VISIBLE
            binding.time.isSelected = true
        }
        if (eventDetail.genres.isNotEmpty()) {
            binding.genres.text = eventDetail.genres
            binding.genresEntry.visibility = View.VISIBLE
            binding.genres.isSelected = true
        }
        if (eventDetail.priceRange.isNotEmpty()) {
            binding.pricerange.text = eventDetail.priceRange
            binding.pricerangeEntry.visibility = View.VISIBLE
            binding.pricerange.isSelected = true
        }
        if (eventDetail.ticketStatus.isNotEmpty()) {
            binding.ticketStatus.text = eventDetail.ticketStatus
            binding.ticketStatus.setBackgroundResource(ticketStatusBgColor(eventDetail.ticketStatus))
            binding.ticketStatusEntry.visibility = View.VISIBLE
        }
        if (eventDetail.ticketURL.isNotEmpty()) {
            binding.ticketUrl.text = eventDetail.ticketURL
            binding.ticketUrlEntry.visibility = View.VISIBLE
            binding.ticketUrl.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            binding.ticketUrl.isSelected = true
            binding.ticketUrl.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binding.ticketUrl.text.toString()))
                startActivity(intent)
            }
        }
        if (eventDetail.seatMapURL.isNotEmpty()) {
            Picasso.get().load(eventDetail.seatMapURL).into(binding.seatmap)
            binding.seatmap.visibility = View.VISIBLE
        }
        return view
    }

    private fun ticketStatusBgColor(ticketStatus: String): Int {
        return when(ticketStatus) {
            "On Sale" -> R.drawable.ticket_status_onsale
            "Off Sale" -> R.drawable.ticket_status_offsale
            "Canceled" -> R.drawable.ticket_status_canceled
            else -> R.drawable.ticket_status_other
        }
    }

}