package com.example.apuniliapp.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.MembershipRequest
import com.example.apuniliapp.data.model.MembershipStatus
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseMembershipRepository
import com.example.apuniliapp.databinding.FragmentAdminMembershipBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminMembershipFragment : Fragment() {

    private var _binding: FragmentAdminMembershipBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MembershipAdapter
    private var currentFilter = MembershipStatus.PENDING
    private var allMemberships: List<MembershipRequest> = emptyList()
    private var searchQuery: String = ""
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMembershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        setupTabs()
        setupRecyclerView()
        setupSearch()
        loadMemberships()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.admin_tab_pending))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.admin_tab_approved))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.admin_tab_rejected))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = when (tab?.position) {
                    0 -> MembershipStatus.PENDING
                    1 -> MembershipStatus.ACCEPTED
                    2 -> MembershipStatus.REJECTED
                    else -> MembershipStatus.PENDING
                }
                setupRecyclerView()
                loadMemberships()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = MembershipAdapter(
            currentFilter = currentFilter,
            onApprove = { membershipId -> showCommentDialog(membershipId, isApproval = true) },
            onReject = { membershipId -> showCommentDialog(membershipId, isApproval = false) },
            onItemClick = { request -> showMembershipDetailDialog(request) }
        )

        binding.rvMembershipRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminMembershipFragment.adapter
        }
    }

    private fun showCommentDialog(membershipId: String, isApproval: Boolean) {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.admin_comment_hint)
            setPadding(64, 32, 64, 16)
        }

        val title = if (isApproval) R.string.admin_confirm_approve else R.string.admin_confirm_reject

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val comment = editText.text.toString().trim()
                if (isApproval) {
                    approveMembership(membershipId, comment)
                } else {
                    rejectMembership(membershipId, comment)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun approveMembership(membershipId: String, comment: String) {
        val sessionManager = SessionManager(requireContext())
        val admin = sessionManager.getLoggedInUser()

        lifecycleScope.launch {
            try {
                val membership = FirebaseMembershipRepository.getMembershipById(membershipId)
                if (membership != null) {
                    // Appel au repository qui met à jour le statut ACCEPTED + membershipStatus
                    FirebaseMembershipRepository.approveMembership(
                        request = membership,
                        comment = comment,
                        reviewerName = admin?.displayName ?: "Admin"
                    )
                    
                    FirebaseAuditLogRepository.logAction(
                        userId = admin?.id ?: "",
                        userName = admin?.displayName ?: "Admin",
                        action = AuditAction.MEMBERSHIP_APPROVED,
                        details = "Adhésion approuvée: ${membership.firstname} ${membership.lastname}" +
                                if (comment.isNotBlank()) " — Commentaire: $comment" else ""
                    )
                    if (isAdded) {
                        Snackbar.make(binding.root, R.string.admin_membership_approved, Snackbar.LENGTH_SHORT).show()
                        loadMemberships()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun rejectMembership(membershipId: String, comment: String) {
        val sessionManager = SessionManager(requireContext())
        val admin = sessionManager.getLoggedInUser()

        lifecycleScope.launch {
            try {
                val membership = FirebaseMembershipRepository.getMembershipById(membershipId)
                FirebaseMembershipRepository.rejectMembership(
                    id = membershipId,
                    userId = membership?.userId ?: "",
                    comment = comment,
                    reviewerName = admin?.displayName ?: "Admin"
                )
                

                FirebaseAuditLogRepository.logAction(
                    userId = admin?.id ?: "",
                    userName = admin?.displayName ?: "Admin",
                    action = AuditAction.MEMBERSHIP_REJECTED,
                    details = "Adhésion refusée: ${membership?.firstname} ${membership?.lastname}" +
                            if (comment.isNotBlank()) " — Motif: $comment" else ""
                )
                if (isAdded) {
                    Snackbar.make(binding.root, R.string.admin_membership_rejected, Snackbar.LENGTH_SHORT).show()
                    loadMemberships()
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showMembershipDetailDialog(request: MembershipRequest) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_membership_detail, null)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)

        // Nom complet
        val fullName = listOf(request.lastname, request.postname, request.firstname)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_fullname).text = fullName

        // Statut
        val chipStatus = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip_detail_status)
        chipStatus.text = when (request.status) {
            MembershipStatus.PENDING -> "⏳ En attente"
            MembershipStatus.ACCEPTED -> "✅ Accepté"
            MembershipStatus.REJECTED -> "❌ Refusé"
        }

        // Informations personnelles
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_gender).text =
            "👤 Sexe : ${request.gender.ifBlank { "Non renseigné" }}"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_birthdate).text =
            "🎂 Né(e) le : ${request.birthdate.ifBlank { "Non renseigné" }}"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_profession).text =
            "💼 Profession : ${request.profession.ifBlank { "Non renseigné" }}"

        // Coordonnées
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_email).text =
            "📧 ${request.email}"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_phone).text =
            "📱 ${request.phone.ifBlank { "Non renseigné" }}"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_address).text =
            "🏠 ${request.address.ifBlank { "Non renseigné" }}"

        // Engagements
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_statutes).text =
            if (request.acceptedStatutes) "✅ Statuts acceptés" else "❌ Statuts non acceptés"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_roi).text =
            if (request.acceptedROI) "✅ ROI accepté" else "❌ ROI non accepté"
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_values).text =
            if (request.acceptedValues) "✅ Valeurs acceptées" else "❌ Valeurs non acceptées"

        // Documents
        val docsInfo = mutableListOf<String>()
        if (request.photoUrl.isNotBlank()) docsInfo.add("📷 Photo fournie")
        if (request.idCardUrl.isNotBlank()) docsInfo.add("🪪 Pièce d'identité fournie")
        if (docsInfo.isEmpty()) docsInfo.add("Aucun document joint")
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_documents_info).text =
            docsInfo.joinToString("\n")

        // Dates
        dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_submitted_at).text =
            "📩 Soumise le : ${if (request.createdAt > 0) dateFormat.format(Date(request.createdAt)) else "Inconnue"}"

        if (request.reviewedAt > 0) {
            dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_reviewed_at).apply {
                visibility = android.view.View.VISIBLE
                text = "📋 Examinée le : ${dateFormat.format(Date(request.reviewedAt))}"
            }
        }

        if (request.reviewedBy.isNotBlank()) {
            dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_reviewed_by).apply {
                visibility = android.view.View.VISIBLE
                text = "👨‍💼 Par : ${request.reviewedBy}"
            }
        }

        // Commentaire admin
        if (request.adminComment.isNotBlank()) {
            dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_admin_comment).visibility =
                android.view.View.VISIBLE
            dialogView.findViewById<android.widget.TextView>(R.id.tv_detail_admin_comment).text =
                request.adminComment
        }

        // Construire la dialog avec actions selon le statut
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Détail de la demande")
            .setView(dialogView)
            .setNegativeButton("Fermer", null)

        // Ajouter les boutons d'action pour les demandes en attente
        if (request.status == MembershipStatus.PENDING) {
            builder.setPositiveButton("✅ Approuver") { _, _ ->
                showCommentDialog(request.id, isApproval = true)
            }
            builder.setNeutralButton("❌ Refuser") { _, _ ->
                showCommentDialog(request.id, isApproval = false)
            }
        }

        builder.show()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce 300ms
                    searchQuery = s?.toString()?.trim() ?: ""
                    filterAndDisplayMemberships()
                }
            }
        })
    }

    private fun filterAndDisplayMemberships() {
        val filtered = if (searchQuery.isBlank()) {
            allMemberships
        } else {
            val query = searchQuery.lowercase()
            allMemberships.filter { request ->
                request.firstname.lowercase().contains(query) ||
                request.lastname.lowercase().contains(query) ||
                request.postname.lowercase().contains(query) ||
                request.email.lowercase().contains(query) ||
                request.phone.contains(query) ||
                request.profession.lowercase().contains(query)
            }
        }

        if (isAdded && _binding != null) {
            adapter.submitList(filtered)

            // Afficher le compteur de résultats si recherche active
            if (searchQuery.isNotBlank()) {
                binding.tvResultCount.visibility = View.VISIBLE
                binding.tvResultCount.text = "${filtered.size} résultat(s) trouvé(s)"
            } else {
                binding.tvResultCount.visibility = View.GONE
            }

            if (filtered.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvMembershipRequests.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvMembershipRequests.visibility = View.VISIBLE
            }
        }
    }

    private fun loadMemberships() {
        lifecycleScope.launch {
            try {
                val memberships = when (currentFilter) {
                    MembershipStatus.PENDING -> FirebaseMembershipRepository.getPendingMemberships()
                    MembershipStatus.ACCEPTED -> FirebaseMembershipRepository.getAllMemberships().filter { it.status == MembershipStatus.ACCEPTED }
                    MembershipStatus.REJECTED -> FirebaseMembershipRepository.getAllMemberships().filter { it.status == MembershipStatus.REJECTED }
                }
                if (isAdded && _binding != null) {
                    allMemberships = memberships
                    filterAndDisplayMemberships()
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur de chargement", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}
