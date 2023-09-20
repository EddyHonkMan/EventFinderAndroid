package com.example.eventfinder

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.eventfinder.databinding.ArtistDetailBinding
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class ResultListAdapter(
    private val events: List<EventData>,
    private val scope: CoroutineScope,
) : RecyclerView.Adapter<ResultListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ShapeableImageView = view.findViewById(R.id.event_icon)
        val eventName: TextView = view.findViewById(R.id.event_name)
        val venueName: TextView = view.findViewById(R.id.venue_name)
        val eventGenre: TextView = view.findViewById(R.id.event_genre)
        val date: TextView = view.findViewById(R.id.date)
        val time: TextView = view.findViewById(R.id.time)
        val like: ImageView = view.findViewById(R.id.like)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_detail, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        Picasso.get().load(event.iconURL).resize(100, 100).centerCrop().into(holder.icon)
//        holder.icon.setImageUrl(event.iconURL, VolleySingleton.imageLoader)
        holder.eventName.text = event.eventName
        holder.eventName.isSelected = true
        holder.venueName.text = event.venueName
        holder.venueName.isSelected = true
        holder.eventGenre.text = event.broadGenre
        holder.date.text = event.date
        holder.time.text = event.time

        holder.itemView.setOnClickListener {
            itemOnClick(it.context, event)
        }

        scope.launch {
            if (FavoriteDataStoreSingleton.hasEvent(event)) {
//            Log.d("hasEvent", event.eventName)
//            Log.d("database", FavoriteDataStoreSingleton.favoriteEvents.toString())
                holder.like.setImageResource(R.drawable.heart_filled)
            } else {
                holder.like.setImageResource(R.drawable.heart_outline)
            }
            holder.like.setOnClickListener {
                scope.launch {
                    if (FavoriteDataStoreSingleton.hasEvent(event)) {
                        holder.like.setImageResource(R.drawable.heart_outline)
                        val index = FavoriteDataStoreSingleton.indexOf(event)
//                        Log.d("remove favorite from result list", "index$index")
                        FavoriteDataStoreSingleton.removeEvent(event)
                        EventMessenger.flow.emit(
                            EventFavorite(
                                eventId = event.eventId,
                                operation = FavoriteOperations.REMOVE,
                                SrcId.RESULT,
                                index
                            )
                        )
                        Log.d(
                            "like button onclick callback context",
                            (it.context as AppCompatActivity).toString()
                        )
                        Utilities.showSnackbar(it, "${event.eventName} removed from favorites")
                    } else {
                        holder.like.setImageResource(R.drawable.heart_filled)
                        FavoriteDataStoreSingleton.addEvent(event)
                        EventMessenger.flow.emit(
                            EventFavorite(
                                eventId = event.eventId,
                                operation = FavoriteOperations.ADD,
                                SrcId.RESULT
                            )
                        )
                        Utilities.showSnackbar(it, "${event.eventName} added to favorites")
                    }
                }
            }
        }
    }

    open fun listenToFavoriteChange() {
        scope.launch {
            EventMessenger.flow.collect {
                if (it.srcId != SrcId.RESULT) {
                    Log.d("result list got event favorite state changed", it.operation.toString())
                    val index = events.indexOfFirst { event -> event.eventId == it.eventId }
                    if (index != -1) {
                        notifyItemChanged(index)
                    }
                }
            }
        }
    }

    fun itemOnClick(context: Context, event: EventData) {
        val intent = Intent(context, EventDetailActivity::class.java)
        intent.putExtra("event", Json.encodeToString(event))
        context.startActivity(intent)
    }
}