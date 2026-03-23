package com.example.apuniliapp.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.data.model.AuditLog
import com.example.apuniliapp.databinding.ItemAuditLogBinding

class AuditLogAdapter : ListAdapter<AuditLog, AuditLogAdapter.ViewHolder>(AuditLogDiffCallback()) {

    class ViewHolder(val binding: ItemAuditLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAuditLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = getItem(position)
        holder.binding.apply {
            tvLogIcon.text = log.action.icon
            tvLogAction.text = log.action.label
            tvLogDetails.text = log.details
            tvLogUser.text = log.userName.ifBlank { "Système" }
            tvLogDate.text = log.formattedDate()
        }
    }

    class AuditLogDiffCallback : DiffUtil.ItemCallback<AuditLog>() {
        override fun areItemsTheSame(oldItem: AuditLog, newItem: AuditLog) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AuditLog, newItem: AuditLog) =
            oldItem == newItem
    }
}

