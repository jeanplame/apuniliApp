package com.example.apuniliapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.data.model.TeamMember
import com.example.apuniliapp.databinding.ItemMemberCardBinding

class TeamMemberAdapter : ListAdapter<TeamMember, TeamMemberAdapter.TeamMemberViewHolder>(TeamMemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
        val binding = ItemMemberCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TeamMemberViewHolder(private val binding: ItemMemberCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: TeamMember) {
            binding.apply {
                tvMemberName.text = member.run { "$firstname $lastname" }
                tvMemberFunction.text = member.position
                tvMemberDescription.text = member.bio
            }
        }
    }

    class TeamMemberDiffCallback : DiffUtil.ItemCallback<TeamMember>() {
        override fun areItemsTheSame(oldItem: TeamMember, newItem: TeamMember): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TeamMember, newItem: TeamMember): Boolean =
            oldItem == newItem
    }
}

