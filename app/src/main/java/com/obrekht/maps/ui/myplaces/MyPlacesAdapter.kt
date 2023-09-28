package com.obrekht.maps.ui.myplaces

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.obrekht.maps.databinding.ItemPlaceBinding
import com.obrekht.maps.model.Place

class MyPlacesAdapter(
    private val interactionListener: PlaceInteractionListener
) : ListAdapter<Place, MyPlacesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding, interactionListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.recycle()
    }

    class ViewHolder(
        val binding: ItemPlaceBinding,
        interactionListener: PlaceInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private var place: Place? = null

        init {
            itemView.setOnClickListener {
                place?.let { place -> interactionListener.onClick(place, it) }
            }
        }

        fun bind(place: Place) {
            this.place = place
            with(binding) {
                title.text = place.name
                description.text = place.description
                description.isVisible = place.description.isNotEmpty()
            }
        }

        fun recycle() {
            place = null
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}

interface PlaceInteractionListener {
    fun onClick(place: Place, view: View) {}
}
