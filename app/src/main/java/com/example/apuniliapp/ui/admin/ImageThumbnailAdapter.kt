package com.example.apuniliapp.ui.admin

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.databinding.ItemImageThumbnailBinding

class ImageThumbnailAdapter(
    private val images: MutableList<Uri>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<ImageThumbnailAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemImageThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri, position: Int) {
            binding.ivThumbnail.setImageURI(uri)
            binding.btnRemoveImage.setOnClickListener {
                onRemove(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageThumbnailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount() = images.size
}

