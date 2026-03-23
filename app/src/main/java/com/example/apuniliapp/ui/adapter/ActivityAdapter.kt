package com.example.apuniliapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.Activity
import com.example.apuniliapp.databinding.ItemActivityCardBinding

class ActivityAdapter(
    private val onItemClick: (Activity) -> Unit = {}
) : ListAdapter<Activity, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActivityViewHolder(
        private val binding: ItemActivityCardBinding,
        private val onItemClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: Activity) {
            binding.apply {
                tvActivityTitle.text = activity.title
                tvActivityDate.text = activity.date
                tvActivityDescription.text = activity.description
                tvActivityLocation.text = activity.location
                chipCategory.text = activity.category.ifEmpty { "Général" }

                // Charger la première image si disponible
                val firstPhoto = activity.photoUrls.firstOrNull()
                if (!firstPhoto.isNullOrBlank()) {
                    ivActivityImage.load(firstPhoto) {
                        crossfade(true)
                        error(R.drawable.ic_volunteer)
                    }
                    ivActivityImage.visibility = View.VISIBLE
                    activityImagePlaceholder.visibility = View.GONE
                } else {
                    ivActivityImage.visibility = View.GONE
                    activityImagePlaceholder.visibility = View.VISIBLE
                }

                root.setOnClickListener { onItemClick(activity) }
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean =
            oldItem == newItem
    }
}
