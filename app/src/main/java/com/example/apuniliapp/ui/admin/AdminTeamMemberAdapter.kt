package com.example.apuniliapp.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.data.model.TeamMember
import com.example.apuniliapp.databinding.ItemAdminTeamMemberBinding

class AdminTeamMemberAdapter(
    private val onEdit: (TeamMember) -> Unit,
    private val onDelete: (TeamMember) -> Unit
) : ListAdapter<TeamMember, AdminTeamMemberAdapter.ViewHolder>(TeamMemberDiffCallback()) {

    class ViewHolder(val binding: ItemAdminTeamMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminTeamMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = getItem(position)
        holder.binding.apply {
            tvTeamName.text = "${member.firstname} ${member.lastname}"
            tvTeamPosition.text = member.position
            tvTeamDepartment.text = member.department.ifBlank { "—" }
            tvTeamContact.text = buildString {
                if (member.email.isNotBlank()) append(member.email)
                if (member.phone.isNotBlank()) {
                    if (isNotBlank()) append(" • ")
                    append(member.phone)
                }
            }.ifBlank { "—" }

            btnEditMember.setOnClickListener { onEdit(member) }
            btnDeleteMember.setOnClickListener { onDelete(member) }
        }
    }

    class TeamMemberDiffCallback : DiffUtil.ItemCallback<TeamMember>() {
        override fun areItemsTheSame(oldItem: TeamMember, newItem: TeamMember) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TeamMember, newItem: TeamMember) =
            oldItem == newItem
    }
}

