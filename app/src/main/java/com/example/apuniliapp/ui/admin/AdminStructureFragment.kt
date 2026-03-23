package com.example.apuniliapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.TeamMember
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseStructureRepository
import com.example.apuniliapp.databinding.FragmentAdminStructureBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AdminStructureFragment : Fragment() {

    private var _binding: FragmentAdminStructureBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminTeamMemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminStructureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        setupRecyclerView()
        setupFab()
        loadMembers()
    }

    private fun setupRecyclerView() {
        adapter = AdminTeamMemberAdapter(
            onEdit = { member -> showMemberDialog(member) },
            onDelete = { member -> confirmDeleteMember(member) }
        )

        binding.rvTeamMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminStructureFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddMember.setOnClickListener {
            showMemberDialog(null)
        }
    }

    private fun showMemberDialog(existingMember: TeamMember?) {
        val isEdit = existingMember != null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_team_member_form, null)

        val etFirstname = dialogView.findViewById<TextInputEditText>(R.id.et_member_firstname)
        val etLastname = dialogView.findViewById<TextInputEditText>(R.id.et_member_lastname)
        val etPosition = dialogView.findViewById<TextInputEditText>(R.id.et_member_position)
        val etDepartment = dialogView.findViewById<TextInputEditText>(R.id.et_member_department)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.et_member_email)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.et_member_phone)
        val etBio = dialogView.findViewById<TextInputEditText>(R.id.et_member_bio)

        if (isEdit) {
            etFirstname.setText(existingMember!!.firstname)
            etLastname.setText(existingMember.lastname)
            etPosition.setText(existingMember.position)
            etDepartment.setText(existingMember.department)
            etEmail.setText(existingMember.email)
            etPhone.setText(existingMember.phone)
            etBio.setText(existingMember.bio)
        }

        val title = if (isEdit) R.string.admin_member_edit else R.string.admin_member_add

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val firstname = etFirstname.text.toString().trim()
                val lastname = etLastname.text.toString().trim()
                val position = etPosition.text.toString().trim()

                if (firstname.isBlank() || lastname.isBlank()) {
                    Snackbar.make(binding.root, R.string.admin_publish_fill_fields, Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val member = TeamMember(
                    id = existingMember?.id ?: "",
                    firstname = firstname,
                    lastname = lastname,
                    position = position,
                    department = etDepartment.text.toString().trim(),
                    email = etEmail.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    bio = etBio.text.toString().trim()
                )

                val sessionManager = SessionManager(requireContext())
                val admin = sessionManager.getLoggedInUser()

                lifecycleScope.launch {
                    try {
                        if (isEdit) {
                            FirebaseStructureRepository.updateTeamMember(existingMember!!.id, member)
                            FirebaseAuditLogRepository.logAction(
                                userId = admin?.id ?: "",
                                userName = admin?.displayName ?: "Admin",
                                action = AuditAction.MEMBER_EDITED,
                                details = "Membre modifié: $firstname $lastname ($position)"
                            )
                        } else {
                            FirebaseStructureRepository.addTeamMember(member)
                            FirebaseAuditLogRepository.logAction(
                                userId = admin?.id ?: "",
                                userName = admin?.displayName ?: "Admin",
                                action = AuditAction.MEMBER_ADDED,
                                details = "Membre ajouté: $firstname $lastname ($position)"
                            )
                        }
                        if (isAdded) {
                            Snackbar.make(binding.root, R.string.admin_member_saved, Snackbar.LENGTH_SHORT).show()
                            loadMembers()
                        }
                    } catch (e: Exception) {
                        if (isAdded) Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDeleteMember(member: TeamMember) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_member_delete_confirm)
            .setMessage("\"${member.firstname} ${member.lastname}\" sera supprimé définitivement.")
            .setPositiveButton(R.string.delete) { _, _ ->
                val sessionManager = SessionManager(requireContext())
                val admin = sessionManager.getLoggedInUser()

                lifecycleScope.launch {
                    try {
                        FirebaseStructureRepository.deleteTeamMember(member.id)
                        FirebaseAuditLogRepository.logAction(
                            userId = admin?.id ?: "",
                            userName = admin?.displayName ?: "Admin",
                            action = AuditAction.MEMBER_DELETED,
                            details = "Membre supprimé: ${member.firstname} ${member.lastname}"
                        )
                        if (isAdded) {
                            Snackbar.make(binding.root, R.string.admin_member_deleted, Snackbar.LENGTH_SHORT).show()
                            loadMembers()
                        }
                    } catch (e: Exception) {
                        if (isAdded) Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun loadMembers() {
        lifecycleScope.launch {
            try {
                val members = FirebaseStructureRepository.getAllTeamMembers()
                if (isAdded) {
                    adapter.submitList(members)
                    if (members.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvTeamMembers.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvTeamMembers.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur de chargement", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
