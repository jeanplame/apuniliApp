package com.example.apuniliapp.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.GalleryItem
import com.example.apuniliapp.databinding.ItemGalleryBinding

class GalleryAdapter : ListAdapter<GalleryItem, GalleryAdapter.ViewHolder>(GalleryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GalleryItem) {
            binding.apply {
                // Title
                tvGalleryTitle.text = item.title.ifBlank { if (item.type == "video") "Vidéo" else "Photo" }

                // Label (type + category)
                tvGalleryLabel.text = buildString {
                    append(if (item.type == "video") "🎬 Vidéo" else "📷 Photo")
                    if (item.category.isNotBlank()) {
                        append(" • ")
                        append(item.category)
                    }
                }

                // Video overlay
                ivPlayOverlay.visibility = if (item.type == "video") View.VISIBLE else View.GONE

                // Load image
                val imageUrl = item.imageUrl.ifBlank { item.videoUrl }
                if (imageUrl.isNotBlank()) {
                    galleryImage.load(imageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_photo_library)
                        error(R.drawable.ic_photo_library)
                    }
                } else {
                    galleryImage.setImageResource(R.drawable.ic_photo_library)
                }

                // Click to open media URL
                root.setOnClickListener {
                    val url = if (item.type == "video") {
                        item.videoUrl.ifBlank { item.imageUrl }
                    } else {
                        item.imageUrl.ifBlank { item.videoUrl }
                    }
                    if (url.isNotBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            it.context.startActivity(intent)
                        } catch (_: Exception) { }
                    }
                }
            }
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem) = oldItem == newItem
    }
}
