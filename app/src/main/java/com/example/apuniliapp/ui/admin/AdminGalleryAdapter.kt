package com.example.apuniliapp.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.GalleryItem
import com.example.apuniliapp.databinding.ItemAdminGalleryBinding

class AdminGalleryAdapter(
    private val onEdit: (GalleryItem) -> Unit = {},
    private val onDelete: (GalleryItem) -> Unit
) : ListAdapter<GalleryItem, AdminGalleryAdapter.ViewHolder>(GalleryDiffCallback()) {

    class ViewHolder(val binding: ItemAdminGalleryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminGalleryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            tvGalleryTitle.text = item.title.ifBlank { "Sans titre" }
            tvGalleryType.text = if (item.type == "video") "🎬 Vidéo" else "📷 Photo"
            chipGalleryCategory.text = item.category.ifBlank { "—" }

            if (item.description.isNotBlank()) {
                tvGalleryDescription.visibility = View.VISIBLE
                tvGalleryDescription.text = item.description
            } else {
                tvGalleryDescription.visibility = View.GONE
            }

            // Load thumbnail
            val imageUrl = item.imageUrl.ifBlank { item.videoUrl }
            if (imageUrl.isNotBlank()) {
                ivGalleryThumbnail.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_photo_library)
                    error(R.drawable.ic_photo_library)
                }
            } else {
                ivGalleryThumbnail.setImageResource(
                    if (item.type == "video") R.drawable.ic_slideshow_black_24dp
                    else R.drawable.ic_photo_library
                )
            }

            // Show play overlay for videos
            ivThumbnailPlay.visibility = if (item.type == "video") View.VISIBLE else View.GONE

            // Edit and delete buttons
            btnEditGallery.setOnClickListener { onEdit(item) }
            btnDeleteGallery.setOnClickListener { onDelete(item) }
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem) =
            oldItem == newItem
    }
}
