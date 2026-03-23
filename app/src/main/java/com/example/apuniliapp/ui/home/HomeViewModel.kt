package com.example.apuniliapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.Activity
import com.example.apuniliapp.data.model.Event
import com.example.apuniliapp.data.repository.firebase.FirebaseActivityRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseEventRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseMembershipRepository
import com.example.apuniliapp.data.repository.firebase.FirebaseStructureRepository
import com.example.apuniliapp.ui.adapter.BannerItem
import com.example.apuniliapp.ui.adapter.ValueItem
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val latestActivities: List<Activity> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val membersCount: Int = 0,
    val activitiesCount: Int = 0,
    val eventsCount: Int = 0,
    val bannerItems: List<BannerItem> = emptyList(),
    val announcements: List<String> = emptyList(),
    val presidentWord: String = "",
    val values: List<ValueItem> = emptyList(),
    val hasAnimatedCounters: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val activities = FirebaseActivityRepository.getAllActivities()
                val events = FirebaseEventRepository.getAllEvents()
                val teamMembers = FirebaseStructureRepository.getAllTeamMembers()
                val approvedMemberships = FirebaseMembershipRepository.getApprovedMemberships()
                val membersCount = teamMembers.size + approvedMemberships.size

                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    latestActivities = activities.sortedByDescending { it.createdAt }.take(5),
                    upcomingEvents = events.take(3),
                    membersCount = if (membersCount > 0) membersCount else 150,
                    activitiesCount = if (activities.isNotEmpty()) activities.size else 25,
                    eventsCount = if (events.isNotEmpty()) events.size else 12,
                    bannerItems = getBannerItems(),
                    announcements = getAnnouncements(),
                    presidentWord = getPresidentWord(),
                    values = getValueItems(),
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Une erreur est survenue"
                )
            }
        }
    }

    fun markCountersAnimated() {
        _uiState.value = _uiState.value?.copy(hasAnimatedCounters = true)
    }

    fun refresh() {
        _uiState.value = _uiState.value?.copy(hasAnimatedCounters = false)
        loadHomeData()
    }

    private fun getBannerItems(): List<BannerItem> {
        return listOf(
            BannerItem(
                title = "Apunili ASBL",
                subtitle = "Solidarité • Entraide • Développement",
                backgroundRes = R.drawable.bg_hero_banner_1
            ),
            BannerItem(
                title = "Ensemble pour un avenir meilleur",
                subtitle = "Rejoignez notre communauté de solidarité",
                backgroundRes = R.drawable.bg_hero_banner_2
            ),
            BannerItem(
                title = "Agissons ensemble",
                subtitle = "Chaque action compte pour le changement",
                backgroundRes = R.drawable.bg_hero_banner_3
            )
        )
    }

    private fun getAnnouncements(): List<String> {
        return listOf(
            "📢 Assemblée générale le 25 Mars 2026 — Tous les membres sont invités !",
            "🎉 Nouvelle campagne de sensibilisation santé en cours à Kinshasa",
            "📋 Les inscriptions pour la formation en entrepreneuriat sont ouvertes"
        )
    }

    private fun getPresidentWord(): String {
        return "Chers membres et sympathisants, Apunili continue de grandir grâce à votre engagement sans faille. " +
                "Ensemble, nous avons touché des centaines de vies cette année. Notre mission de solidarité et " +
                "d'entraide reste plus que jamais notre boussole. Je vous invite à rester mobilisés pour les projets " +
                "à venir et à accueillir chaleureusement nos nouveaux membres. L'avenir d'Apunili, c'est nous tous."
    }

    private fun getValueItems(): List<ValueItem> {
        return listOf(
            ValueItem(R.drawable.ic_solidarity, "Solidarité", "S'entraider pour avancer ensemble"),
            ValueItem(R.drawable.ic_heart, "Entraide", "Tendre la main à ceux qui en ont besoin"),
            ValueItem(R.drawable.ic_transparency, "Transparence", "Agir avec honnêteté et intégrité"),
            ValueItem(R.drawable.ic_engagement, "Engagement", "Se consacrer pleinement à notre cause")
        )
    }
}