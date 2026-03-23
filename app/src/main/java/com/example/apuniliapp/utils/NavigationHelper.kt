package com.example.apuniliapp.utils

import androidx.navigation.NavController
import com.example.apuniliapp.R

object NavigationHelper {
    fun navigateToHome(navController: NavController) {
        navController.navigate(R.id.nav_home)
    }

    fun navigateToLogin(navController: NavController) {
        navController.navigate(R.id.nav_login)
    }

    fun navigateToAdminDashboard(navController: NavController) {
        navController.navigate(R.id.nav_admin_dashboard)
    }

    fun navigateToProfile(navController: NavController) {
        navController.navigate(R.id.nav_settings)
    }

    fun navigateToMembership(navController: NavController) {
        navController.navigate(R.id.nav_membership)
    }

    fun navigateToContact(navController: NavController) {
        navController.navigate(R.id.nav_contact)
    }

    fun navigateSafe(navController: NavController, actionId: Int): Boolean {
        return try {
            navController.navigate(actionId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

