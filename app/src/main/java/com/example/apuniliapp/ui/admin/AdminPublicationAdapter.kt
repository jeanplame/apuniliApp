package com.example.apuniliapp.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.R
import com.example.apuniliapp.databinding.ItemAdminPublicationBinding

/**
 * Item générique qui peut représenter une Activité ou un Événement
 */
data class PublicationItem(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String = "",
    val imageCount: Int = 0,
    val isActivity: Boolean = true
)

class AdminPublicationAdapter(
    private val onEdit: (PublicationItem) -> Unit,
    private val onDelete: (PublicationItem) -> Unit
) : ListAdapter<PublicationItem, AdminPublicationAdapter.ViewHolder>(PublicationDiffCallback()) {

    class ViewHolder(val binding: ItemAdminPublicationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminPublicationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            tvPubTitle.text = item.title
            tvPubDate.text = item.date
            tvPubDescription.text = item.description
            tvPubLocation.text = "📍 ${item.location}"

            // Type chip
            if (item.isActivity) {
                chipPubType.text = item.category.ifBlank { "Activité" }
                ivPubIcon.setImageResource(R.drawable.ic_event)
            } else {
                chipPubType.text = "Événement"
                ivPubIcon.setImageResource(R.drawable.ic_event)
            }

            // Images count
            tvPubImagesCount.text = "${item.imageCount} 📷"

            btnEditPub.setOnClickListener { onEdit(item) }
            btnDeletePub.setOnClickListener { onDelete(item) }
        }
    }

    class PublicationDiffCallback : DiffUtil.ItemCallback<PublicationItem>() {
        override fun areItemsTheSame(oldItem: PublicationItem, newItem: PublicationItem) =
            oldItem.id == newItem.id && oldItem.isActivity == newItem.isActivity
        override fun areContentsTheSame(oldItem: PublicationItem, newItem: PublicationItem) =
            oldItem == newItem
    }
}

