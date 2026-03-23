package com.example.apuniliapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.appcompat.app.AppCompatActivity
import com.example.apuniliapp.config.SupabaseClient
import com.example.apuniliapp.data.model.AuditAction
import com.example.apuniliapp.data.model.UserRole
import com.example.apuniliapp.data.repository.firebase.FirebaseAuditLogRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseUserRepository
import com.example.apuniliapp.databinding.ActivityMainBinding
import com.example.apuniliapp.utils.FirebaseDiagnostic
import com.example.apuniliapp.utils.SessionManager
import com.example.apuniliapp.utils.SessionRefreshWorker
import com.example.apuniliapp.utils.SupabaseSeeder
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate START")

        sessionManager = SessionManager(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("MainActivity", "setContentView OK")

        setSupportActionBar(binding.appBarMain.toolbar)

        Log.d("MainActivity", "setSupportActionBar OK")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        Log.d("MainActivity", "navController OK")

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_about, R.id.nav_structure,
                R.id.nav_activities, R.id.nav_events, R.id.nav_gallery,
                R.id.nav_documents, R.id.nav_membership, R.id.nav_contact,
                R.id.nav_admin_dashboard
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // === DRAWER: navigation manuelle ===
        binding.navView.setNavigationItemSelectedListener { item ->
            try {
                navController.navigate(
                    item.itemId,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, inclusive = false)
                        .setLaunchSingleTop(true)
                        .build()
                )
            } catch (e: Exception) {
                Log.w("MainActivity", "Drawer nav error: ${e.message}")
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        val bottomNavView = binding.appBarMain.contentMain.bottomNavView

        // === BOTTOM NAV: navigation manuelle ===
        bottomNavView?.setOnItemSelectedListener { item ->
            val currentDest = navController.currentDestination?.id
            if (currentDest == item.itemId) return@setOnItemSelectedListener true
            try {
                navController.navigate(
                    item.itemId,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, inclusive = false)
                        .setLaunchSingleTop(true)
                        .build()
                )
                true
            } catch (_: Exception) { false }
        }

        // Synchroniser le highlight du bottom nav + drawer avec la destination courante
        navController.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu()
            // Sync bottom nav
            bottomNavView?.menu?.let { menu ->
                for (i in 0 until menu.size()) {
                    val menuItem = menu.getItem(i)
                    if (menuItem.itemId == destination.id) {
                        menuItem.isChecked = true
                        break
                    }
                }
            }
            // Sync drawer
            binding.navView.setCheckedItem(destination.id)
        }

        // Mise à jour initiale de la navigation
        updateNavigationForRole()

        // ✅ DÉMARRER LE RENOUVELLEMENT AUTOMATIQUE DE SESSION
        if (sessionManager.isLoggedIn()) {
            SessionRefreshWorker.startPeriodicRefresh(this)
            Log.d("MainActivity", "Renouvellement de session activé")
        }

        // Initialiser les données de démonstration + diagnostic + rafraîchir profil
        lifecycleScope.launch {
            SupabaseSeeder.seedIfNeeded(this@MainActivity)

            // Diagnostic Supabase — s'affiche si Auth ou Postgrest échouent
            try {
                val result = FirebaseDiagnostic.runDiagnostics()
                val coreOk = result.authEmailEnabled && result.firestoreWritable
                if (!coreOk) {
                    runOnUiThread {
                        FirebaseDiagnostic.showResultDialog(this@MainActivity, result)
                    }
                } else {
                    Log.d("MainActivity", "✅ Supabase diagnostic: core OK (storage=${result.storageWritable})")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Diagnostic error: ${e.message}")
            }

            refreshUserProfile()
        }

        Log.d("MainActivity", "onCreate COMPLETE")
    }

    /**
     * Met à jour dynamiquement le menu de navigation (drawer, header, bottom nav)
     * en fonction du rôle de l'utilisateur actuel.
     */
    fun updateNavigationForRole() {
        val role = sessionManager.getCurrentRole()
        val user = sessionManager.getLoggedInUser()
        val isLoggedIn = sessionManager.isLoggedIn()

        Log.d("MainActivity", "updateNavigationForRole: role=$role, isLoggedIn=$isLoggedIn")

        // === 1. Drawer: Masquer/afficher les sections selon le rôle ===
        val navMenu = binding.navView.menu

        // Section "Espace membre" : visible seulement pour Membre et Admin
        val memberSection = navMenu.findItem(R.id.nav_member_section)
        memberSection?.isVisible = (role == UserRole.MEMBER || role == UserRole.ADMIN)

        // Section "Administration" : visible seulement pour Admin
        val adminSection = navMenu.findItem(R.id.nav_admin_section)
        adminSection?.isVisible = (role == UserRole.ADMIN)

        // === 2. Bottom Navigation : masquer Adhésion pour les visiteurs ===
        val bottomNavView = binding.appBarMain.contentMain.bottomNavView
        bottomNavView?.menu?.findItem(R.id.nav_membership)?.isVisible =
            (role == UserRole.MEMBER || role == UserRole.ADMIN)

        // === 3. Mettre à jour le header de navigation ===
        updateNavHeader(role, user?.displayName, isLoggedIn)

        // === 4. Rafraîchir le menu overflow ===
        invalidateOptionsMenu()
    }

    /**
     * Met à jour le header du drawer avec les infos utilisateur
     */
    private fun updateNavHeader(role: UserRole, displayName: String?, isLoggedIn: Boolean) {
        val headerView = binding.navView.getHeaderView(0) ?: return

        val tvTitle = headerView.findViewById<TextView>(R.id.tv_nav_header_title)
        val tvSubtitle = headerView.findViewById<TextView>(R.id.tv_nav_header_subtitle)
        val tvRole = headerView.findViewById<TextView>(R.id.tv_nav_header_role)

        if (isLoggedIn && displayName != null) {
            tvTitle?.text = displayName
            tvSubtitle?.text = when (role) {
                UserRole.ADMIN -> getString(R.string.role_admin)
                UserRole.MEMBER -> getString(R.string.role_member)
                else -> getString(R.string.nav_header_subtitle)
            }
            tvRole?.text = when (role) {
                UserRole.ADMIN -> getString(R.string.role_admin_label)
                UserRole.MEMBER -> getString(R.string.role_member_label)
                else -> getString(R.string.role_visitor)
            }
        } else {
            tvTitle?.text = getString(R.string.nav_header_title)
            tvSubtitle?.text = getString(R.string.nav_header_subtitle)
            tvRole?.text = getString(R.string.role_visitor_label)
        }
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir la session ET la navigation à chaque retour au premier plan
        lifecycleScope.launch {
            // D'abord, tenter de renouveler la session si nécessaire
            if (sessionManager.isLoggedIn()) {
                try {
                    sessionManager.refreshSessionIfNeeded()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Erreur refresh session: ${e.message}")
                }
            }
            // Ensuite, mettre à jour la navigation sur le thread UI
            runOnUiThread {
                updateNavigationForRole()
            }
            // Enfin, rafraîchir depuis Firestore en arrière-plan
            refreshUserProfile()
        }
    }

    /**
     * Rafraîchit le profil utilisateur depuis Supabase et met à jour le cache local.
     */
    private suspend fun refreshUserProfile() {
        try {
            val supabaseUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
            val freshProfile = FirebaseUserRepository.getUserByUidFromServer(supabaseUser.id) ?: return
            val cachedUser = sessionManager.getLoggedInUser()

            // Si le rôle a changé, mettre à jour le cache et la navigation
            if (cachedUser == null || cachedUser.role != freshProfile.role) {
                Log.d("MainActivity", "Rôle mis à jour: ${cachedUser?.role} → ${freshProfile.role}")
                sessionManager.saveSession(freshProfile)
                runOnUiThread {
                    updateNavigationForRole()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur refreshUserProfile: ${e.message}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isLoggedIn = sessionManager.isLoggedIn()
        val role = sessionManager.getCurrentRole()

        // Afficher Login seulement si non connecté
        menu.findItem(R.id.nav_login)?.isVisible = !isLoggedIn
        // Afficher Déconnexion seulement si connecté
        menu.findItem(R.id.nav_logout)?.isVisible = isLoggedIn

        // Afficher le menu "Mon Espace" seulement pour les membres et admins connectés
        menu.findItem(R.id.nav_member_menu)?.isVisible = 
            isLoggedIn && (role == UserRole.MEMBER || role == UserRole.ADMIN)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            when (item.itemId) {
                R.id.nav_settings -> {
                    navController.navigate(R.id.nav_settings)
                    true
                }
                R.id.nav_login -> {
                    navController.navigate(R.id.nav_login)
                    true
                }
                R.id.nav_logout -> {
                    performLogout()
                    true
                }
                // Member Menu Actions — naviguer vers les destinations directement
                R.id.nav_member_profile -> {
                    navController.navigate(R.id.nav_member_profile_edit)
                    true
                }
                R.id.nav_member_history -> {
                    navController.navigate(R.id.nav_member_history_detail)
                    true
                }
                R.id.nav_member_settings -> {
                    navController.navigate(R.id.nav_member_settings)
                    true
                }
                R.id.nav_member_gallery -> {
                    navController.navigate(R.id.nav_gallery)
                    true
                }
                R.id.nav_member_documents -> {
                    navController.navigate(R.id.nav_documents)
                    true
                }
                R.id.nav_member_events -> {
                    navController.navigate(R.id.nav_events)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Navigation error: ${e.message}")
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Effectue la déconnexion : log d'audit, nettoyage session, navigation vers Home
     */
    private fun performLogout() {
        val user = sessionManager.getLoggedInUser()
        if (user != null) {
            lifecycleScope.launch {
                FirebaseAuditLogRepository.logAction(
                    userId = user.id,
                    userName = user.displayName,
                    action = AuditAction.LOGOUT,
                    details = "Déconnexion depuis le menu"
                )
            }
        }

        sessionManager.logout()
        
        // Arrêter le renouvellement automatique de session
        SessionRefreshWorker.stopPeriodicRefresh(this)

        // Mettre à jour la navigation immédiatement
        updateNavigationForRole()
        invalidateOptionsMenu()

        // Naviguer vers l'accueil en nettoyant la pile de navigation
        try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_home,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .build()
            )
        } catch (_: Exception) { }

        Snackbar.make(binding.root, getString(R.string.logout_success), Snackbar.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}