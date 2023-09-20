package com.example.eventfinder

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FavoriteListAdapter(
    private var events: MutableList<EventData>,
    private val scope: CoroutineScope,
    private val recyclerView: RecyclerView
) : ResultListAdapter(events, scope) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        Picasso.get().load(event.iconURL).resize(100, 100).centerCrop().into(holder.icon)
        holder.eventName.text = event.eventName
        holder.eventName.isSelected = true
        holder.venueName.text = event.venueName
        holder.venueName.isSelected = true
        holder.eventGenre.text = event.broadGenre
        holder.date.text = event.date
        holder.time.text = event.time
        holder.like.setImageResource(R.drawable.heart_filled)
        holder.itemView.setOnClickListener {
            itemOnClick(it.context, event)
        }
        holder.like.setOnClickListener{
            holder.like.setImageResource(R.drawable.heart_outline)
            scope.launch {
                val index = events.indexOfFirst { it.eventId == event.eventId }
                FavoriteDataStoreSingleton.removeEvent(event)
                notifyItemRemoved(index)

                Utilities.showSnackbar(it, "${event.eventName} removed from favorites")

                if (events.isEmpty()) {
                    recyclerView.visibility = View.GONE
                }
                EventMessenger.flow.emit(EventFavorite(event.eventId, FavoriteOperations.REMOVE, SrcId.FAVORITE))
            }
        }
    }

    override fun listenToFavoriteChange() {
        scope.launch {
            EventMessenger.flow.collect{
                if (it.srcId != SrcId.FAVORITE) {
                    Log.d("favorite list got event favorite state changed", it.operation.toString())
                    if (it.operation == FavoriteOperations.ADD) {
                        notifyItemInserted(events.size - 1)
                        recyclerView.visibility = View.VISIBLE
                    }
                    else {
                        if (it.originalIndexInFavoriteList != -1) {
                            notifyItemRemoved(it.originalIndexInFavoriteList)
                            if (events.isEmpty()) {
                                recyclerView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }
}