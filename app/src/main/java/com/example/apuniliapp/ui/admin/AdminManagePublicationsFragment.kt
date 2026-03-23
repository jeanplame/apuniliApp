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
import com.example.apuniliapp.data.repository.firebase.FirebaseActivityRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseEventRepository
import com.example.apuniliapp.databinding.FragmentAdminManagePublicationsBinding
import com.example.apuniliapp.utils.AdminAccessGuard
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * Fragment pour lister, modifier et supprimer les activités/événements publiés.
 * Le bouton FAB redirige vers AdminPublishFragment pour créer.
 * Le bouton "Modifier" redirige vers AdminPublishFragment en mode édition.
 */
class AdminManagePublicationsFragment : Fragment() {

    private var _binding: FragmentAdminManagePublicationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminPublicationAdapter
    private var showActivities = true // true = Activités, false = Événements

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManagePublicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!AdminAccessGuard.checkAdminAccess(requireContext(), findNavController(), binding.root)) {
            return
        }

        setupTabs()
        setupRecyclerView()
        setupFab()
        loadPublications()
    }

    private fun setupTabs() {
        binding.tabLayoutPublications.addTab(binding.tabLayoutPublications.newTab().setText("Activités"))
        binding.tabLayoutPublications.addTab(binding.tabLayoutPublications.newTab().setText("Événements"))

        binding.tabLayoutPublications.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showActivities = tab?.position == 0
                loadPublications()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = AdminPublicationAdapter(
            onEdit = { item -> navigateToEdit(item) },
            onDelete = { item -> confirmDelete(item) }
        )

        binding.rvPublications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdminManagePublicationsFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddPublication.setOnClickListener {
            // Naviguer vers le formulaire de publication (mode création)
            try {
                findNavController().navigate(R.id.action_manage_publications_to_publish)
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Navigation impossible", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToEdit(item: PublicationItem) {
        try {
            val bundle = Bundle().apply {
                putString("editId", item.id)
                putBoolean("isActivity", item.isActivity)
            }
            findNavController().navigate(R.id.action_manage_publications_to_publish, bundle)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Navigation impossible", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(item: PublicationItem) {
        val typeLabel = if (item.isActivity) "l'activité" else "l'événement"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer $typeLabel")
            .setMessage("\"${item.title}\" sera supprimé définitivement.")
            .setPositiveButton("Supprimer") { _, _ -> deletePublication(item) }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deletePublication(item: PublicationItem) {
        val sessionManager = SessionManager(requireContext())
        val admin = sessionManager.getLoggedInUser()

        lifecycleScope.launch {
            try {
                if (item.isActivity) {
                    FirebaseActivityRepository.deleteActivity(item.id)
                    FirebaseAuditLogRepository.logAction(
                        userId = admin?.id ?: "",
                        userName = admin?.displayName ?: "Admin",
                        action = AuditAction.ACTIVITY_DELETED,
                        details = "Activité supprimée: ${item.title}"
                    )
                } else {
                    FirebaseEventRepository.deleteEvent(item.id)
                    FirebaseAuditLogRepository.logAction(
                        userId = admin?.id ?: "",
                        userName = admin?.displayName ?: "Admin",
                        action = AuditAction.EVENT_DELETED,
                        details = "Événement supprimé: ${item.title}"
                    )
                }
                if (isAdded) {
                    Snackbar.make(binding.root, "Supprimé avec succès", Snackbar.LENGTH_SHORT).show()
                    loadPublications()
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadPublications() {
        lifecycleScope.launch {
            try {
                val items = if (showActivities) {
                    FirebaseActivityRepository.getAllActivities().map { activity ->
                        PublicationItem(
                            id = activity.id,
                            title = activity.title,
                            description = activity.description,
                            date = activity.date,
                            location = activity.location,
                            category = activity.category,
                            imageCount = activity.photoUrls.size,
                            isActivity = true
                        )
                    }
                } else {
                    FirebaseEventRepository.getAllEvents().map { event ->
                        PublicationItem(
                            id = event.id,
                            title = event.title,
                            description = event.description,
                            date = event.date,
                            location = event.location,
                            imageCount = event.photoUrls.size + event.videoUrls.size,
                            isActivity = false
                        )
                    }
                }

                if (isAdded) {
                    adapter.submitList(items)
                    if (items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvPublications.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvPublications.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                if (isAdded) Snackbar.make(binding.root, "Erreur de chargement", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recharger quand on revient du formulaire d'édition
        loadPublications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

