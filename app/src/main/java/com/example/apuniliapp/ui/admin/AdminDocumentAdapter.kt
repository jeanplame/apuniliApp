package com.example.apuniliapp.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.data.model.Document
import com.example.apuniliapp.databinding.ItemAdminDocumentBinding

class AdminDocumentAdapter(
    private val onEdit: (Document) -> Unit,
    private val onDelete: (Document) -> Unit
) : ListAdapter<Document, AdminDocumentAdapter.ViewHolder>(DocumentDiffCallback()) {

    class ViewHolder(val binding: ItemAdminDocumentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminDocumentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val document = getItem(position)
        holder.binding.apply {
            tvDocTitle.text = document.title
            tvDocCategory.text = document.category.label
            chipDocType.text = document.type.ifBlank { "PDF" }
            tvDocSize.text = document.fileSize.ifBlank { "—" }

            if (document.description.isNotBlank()) {
                tvDocDescription.visibility = View.VISIBLE
                tvDocDescription.text = document.description
            } else {
                tvDocDescription.visibility = View.GONE
            }

            btnEditDoc.setOnClickListener { onEdit(document) }
            btnDeleteDoc.setOnClickListener { onDelete(document) }
        }
    }

    class DocumentDiffCallback : DiffUtil.ItemCallback<Document>() {
        override fun areItemsTheSame(oldItem: Document, newItem: Document) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Document, newItem: Document) =
            oldItem == newItem
    }
}

