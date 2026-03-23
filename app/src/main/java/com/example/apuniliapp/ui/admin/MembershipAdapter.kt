package com.example.apuniliapp.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.data.model.MembershipRequest
import com.example.apuniliapp.data.model.MembershipStatus
import com.example.apuniliapp.databinding.ItemAdminMembershipBinding

class MembershipAdapter(
    private val currentFilter: MembershipStatus = MembershipStatus.PENDING,
    private val onApprove: (String) -> Unit = {},
    private val onReject: (String) -> Unit = {},
    private val onItemClick: (MembershipRequest) -> Unit = {}
) : ListAdapter<MembershipRequest, MembershipAdapter.ViewHolder>(MembershipDiffCallback()) {

    class ViewHolder(val binding: ItemAdminMembershipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMembershipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = getItem(position)
        holder.binding.apply {
            tvMemberName.text = "${request.firstname} ${request.lastname}"
            tvMemberEmail.text = request.email
            tvMemberInfo.text = "${request.profession} • ${request.phone}"

            chipStatus.text = when(request.status) {
                MembershipStatus.PENDING -> "En attente"
                MembershipStatus.ACCEPTED -> "Accepté"
                MembershipStatus.REJECTED -> "Refusé"
            }

            // Afficher les boutons seulement pour les demandes en attente
            val showActions = currentFilter == MembershipStatus.PENDING
            btnApprove.visibility = if (showActions) View.VISIBLE else View.GONE
            btnReject.visibility = if (showActions) View.VISIBLE else View.GONE

            btnApprove.setOnClickListener { onApprove(request.id) }
            btnReject.setOnClickListener { onReject(request.id) }

            // Clic sur l'item pour voir le détail
            root.setOnClickListener { onItemClick(request) }
        }
    }

    class MembershipDiffCallback : DiffUtil.ItemCallback<MembershipRequest>() {
        override fun areItemsTheSame(oldItem: MembershipRequest, newItem: MembershipRequest) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MembershipRequest, newItem: MembershipRequest) =
            oldItem == newItem
    }
}
