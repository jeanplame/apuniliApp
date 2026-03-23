package com.example.apuniliapp.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.OrganizationInfo
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseOrganizationRepository
import com.example.apuniliapp.databinding.FragmentAdminInfoBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminInfoFragment : Fragment() {

    private var _binding: FragmentAdminInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        loadCurrentInfo()
        setupDatePicker()
        setupSaveButton()
    }

    private fun loadCurrentInfo() {
        lifecycleScope.launch {
            try {
                val info = FirebaseOrganizationRepository.getOrganizationInfo()
                if (isAdded && _binding != null) {
                    binding.apply {
                        etOrgName.setText(info.name)
                        etOrgMission.setText(info.mission)
                        etOrgVision.setText(info.vision)
                        etOrgObjectives.setText(info.objectives)
                        etOrgHistory.setText(info.history)
                        etOrgAddress.setText(info.address)
                        etOrgPhone.setText(info.phone)
                        etOrgEmail.setText(info.email)
                        etOrgWebsite.setText(info.website)
                        etOrgFoundedDate.setText(info.foundedDate)
                    }
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur de chargement", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDatePicker() {
        binding.etOrgFoundedDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val cal = Calendar.getInstance().apply { set(year, month, day) }
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                    binding.etOrgFoundedDate.setText(format.format(cal.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveInfo.setOnClickListener {
            val name = binding.etOrgName.text.toString().trim()
            if (name.isBlank()) {
                Snackbar.make(binding.root, "Le nom de l'association est obligatoire", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedInfo = OrganizationInfo(
                id = "main",
                name = name,
                mission = binding.etOrgMission.text.toString().trim(),
                vision = binding.etOrgVision.text.toString().trim(),
                objectives = binding.etOrgObjectives.text.toString().trim(),
                history = binding.etOrgHistory.text.toString().trim(),
                address = binding.etOrgAddress.text.toString().trim(),
                phone = binding.etOrgPhone.text.toString().trim(),
                email = binding.etOrgEmail.text.toString().trim(),
                website = binding.etOrgWebsite.text.toString().trim(),
                foundedDate = binding.etOrgFoundedDate.text.toString().trim()
            )

            binding.btnSaveInfo.isEnabled = false
            binding.btnSaveInfo.text = "Sauvegarde..."

            lifecycleScope.launch {
                try {
                    FirebaseOrganizationRepository.updateOrganizationInfo(updatedInfo)

                    val sessionManager = SessionManager(requireContext())
                    val admin = sessionManager.getLoggedInUser()
                    FirebaseAuditLogRepository.logAction(
                        userId = admin?.id ?: "",
                        userName = admin?.displayName ?: "Admin",
                        action = AuditAction.INFO_MODIFIED,
                        details = "Informations de l'association mises à jour"
                    )

                    if (isAdded && _binding != null) {
                        Snackbar.make(binding.root, R.string.admin_info_saved, Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    if (isAdded && _binding != null) {
                        Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                } finally {
                    if (isAdded && _binding != null) {
                        binding.btnSaveInfo.isEnabled = true
                        binding.btnSaveInfo.text = getString(R.string.save)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
