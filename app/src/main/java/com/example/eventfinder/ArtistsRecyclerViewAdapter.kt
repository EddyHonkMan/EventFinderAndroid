package com.example.eventfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventfinder.databinding.ArtistDetailBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope

class ArtistsRecyclerViewAdapter(
    private val artists: MutableList<MusicRelatedArtistData>,
    private val scope: CoroutineScope? = null,
) : RecyclerView.Adapter<ArtistsRecyclerViewAdapter.ArtistViewHolder>() {
    inner class ArtistViewHolder(val binding: ArtistDetailBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ArtistDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        with(holder) {
            val albumBindings = listOf(binding.album1, binding.album2, binding.album3)
            with(artists[position]) {
                Picasso.get().load(artistIconURL).into(binding.artistIcon)
                for(i in albumBindings.indices) {
                    if (i < albumURLs.size) {
                        Picasso.get().load(albumURLs[i]).into(albumBindings[i])
                    }
                    else {
                        albumBindings[i].visibility = View.GONE
                    }
                }
                binding.artistName.text = artistName

                binding.followers.text = followerNumberConvert(numOfFollowers) + " Followers"

                binding.spotifyLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                binding.spotifyLink.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyLink))
                    it.context.startActivity(intent)
                }

                binding.popularityText.text = popularity
                binding.popularityProgressbar.progress = popularity.toInt()
            }
        }
    }

    private fun followerNumberConvert(follower: String): String{
        return when(val num = follower.toInt()) {
            in 0..999 -> follower
            in 1000..999999 -> (num / 1000).toString() + "K"
            else -> (num / 1000000).toString() + "M"
        }
    }
}