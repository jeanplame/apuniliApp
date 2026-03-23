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
import com.example.apuniliapp.databinding.ItemActivityListBinding

class ActivityListAdapter(
    private val onItemClick: (Activity) -> Unit = {},
    private val onShareClick: (Activity) -> Unit = {}
) : ListAdapter<Activity, ActivityListAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemActivityListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(
        private val binding: ItemActivityListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: Activity) {
            binding.apply {
                tvActivityTitle.text = activity.title
                tvActivityDate.text = activity.date
                tvActivityLocation.text = activity.location
                tvActivityDescription.text = activity.description

                // Charger la première image
                val firstPhoto = activity.photoUrls.firstOrNull()
                if (!firstPhoto.isNullOrBlank()) {
                    activityImage.load(firstPhoto) {
                        crossfade(true)
                        error(R.drawable.ic_volunteer)
                    }
                }

                if (activity.category.isNotBlank()) {
                    chipCategory.text = activity.category
                    chipCategory.visibility = View.VISIBLE
                } else {
                    chipCategory.visibility = View.GONE
                }

                root.setOnClickListener { onItemClick(activity) }
                btnShare.setOnClickListener { onShareClick(activity) }
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
