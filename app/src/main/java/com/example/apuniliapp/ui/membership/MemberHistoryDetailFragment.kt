package com.example.apuniliapp.ui.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.R
import com.example.apuniliapp.databinding.FragmentMemberHistoryDetailBinding
import com.example.apuniliapp.data.model.MembershipHistory
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment affichant l'historique détaillé de l'adhésion avec timeline
 */
class MemberHistoryDetailFragment : Fragment() {

    private var _binding: FragmentMemberHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MembershipViewModel by viewModels()
    private lateinit var adapter: MembershipHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupToolbar()
        
        // Charger les données d'historique
        val sessionManager = SessionManager(requireContext())
        val currentUser = sessionManager.getLoggedInUser()
        if (currentUser != null) {
            viewModel.loadMembershipHistory(currentUser.id)
        }
    }

    private fun setupRecyclerView() {
        adapter = MembershipHistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MemberHistoryDetailFragment.adapter
        }
    }

    private fun setupToolbar() {
        binding.toolbarHistory.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.membershipHistory.observe(viewLifecycleOwner) { history ->
            if (history.isNotEmpty()) {
                binding.rvHistory.visibility = View.VISIBLE
                binding.tvEmptyHistory.visibility = View.GONE
                adapter.submitList(history)
            } else {
                binding.rvHistory.visibility = View.GONE
                binding.tvEmptyHistory.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Adapter pour afficher les événements d'adhésion en timeline
 */
class MembershipHistoryAdapter : RecyclerView.Adapter<MembershipHistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<MembershipHistory>()

    fun submitList(list: List<MembershipHistory>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_membership_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate = itemView.findViewById<android.widget.TextView>(R.id.tv_history_date)
        private val tvEvent = itemView.findViewById<android.widget.TextView>(R.id.tv_history_event)
        private val tvDescription = itemView.findViewById<android.widget.TextView>(R.id.tv_history_description)
        private val ivIcon = itemView.findViewById<android.widget.ImageView>(R.id.iv_history_icon)

        fun bind(history: MembershipHistory) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.FRANCE)
            tvDate.text = dateFormat.format(Date(history.createdAt))
            tvEvent.text = getEventTitle(history.eventType)
            tvDescription.text = history.description.ifBlank { getEventDescription(history.eventType) }
            ivIcon.setImageResource(getEventIcon(history.eventType))
            ivIcon.setColorFilter(getEventColor(history.eventType, itemView.context))
        }

        private fun getEventTitle(eventType: String): String = when (eventType) {
            "SUBMITTED" -> "✏️ Demande soumise"
            "APPROVED" -> "✅ Demande approuvée"
            "REJECTED" -> "❌ Demande rejetée"
            "RENEWED" -> "🔄 Adhésion renouvelée"
            "SUSPENDED" -> "⏸️ Adhésion suspendue"
            "EXPIRED" -> "⏱️ Adhésion expirée"
            else -> "📌 Événement"
        }

        private fun getEventDescription(eventType: String): String = when (eventType) {
            "SUBMITTED" -> "Vous avez soumis votre demande d'adhésion"
            "APPROVED" -> "Félicitations! Votre adhésion a été approuvée"
            "REJECTED" -> "Votre demande d'adhésion a été rejetée"
            "RENEWED" -> "Votre adhésion a été renouvelée avec succès"
            "SUSPENDED" -> "Votre adhésion a été suspendue"
            "EXPIRED" -> "Votre adhésion a expiré"
            else -> "Événement d'adhésion"
        }

        private fun getEventIcon(eventType: String): Int = when (eventType) {
            "SUBMITTED" -> R.drawable.ic_activities
            "APPROVED" -> R.drawable.ic_info
            "REJECTED" -> R.drawable.ic_close
            "RENEWED" -> R.drawable.ic_activities
            "SUSPENDED" -> R.drawable.ic_info
            "EXPIRED" -> R.drawable.ic_calendar
            else -> R.drawable.ic_info
        }

        private fun getEventColor(eventType: String, context: android.content.Context): Int = when (eventType) {
            "SUBMITTED" -> context.getColor(android.R.color.holo_blue_dark)
            "APPROVED" -> context.getColor(android.R.color.holo_green_dark)
            "REJECTED" -> context.getColor(android.R.color.holo_red_dark)
            "RENEWED" -> context.getColor(android.R.color.holo_green_dark)
            "SUSPENDED" -> context.getColor(android.R.color.holo_orange_dark)
            "EXPIRED" -> context.getColor(android.R.color.holo_red_dark)
            else -> context.getColor(android.R.color.darker_gray)
        }
    }
}
