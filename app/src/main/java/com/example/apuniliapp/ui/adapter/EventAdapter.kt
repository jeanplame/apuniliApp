package com.example.apuniliapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.data.model.Event
import com.example.apuniliapp.databinding.ItemEventCardBinding

class EventAdapter(
    private val onItemClick: (Event) -> Unit = {}
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: ItemEventCardBinding,
        private val onItemClick: (Event) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.apply {
                tvEventTitle.text = event.title
                tvEventDescription.text = event.description
                tvEventLocation.text = event.location

                // Parse date to extract day and month (format: "10 Avr 2024")
                val dateParts = event.date.split(" ")
                if (dateParts.size >= 2) {
                    tvEventDay.text = dateParts[0]
                    tvEventMonth.text = dateParts[1].uppercase()
                } else {
                    tvEventDay.text = "--"
                    tvEventMonth.text = "---"
                }

                root.setOnClickListener { onItemClick(event) }
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
