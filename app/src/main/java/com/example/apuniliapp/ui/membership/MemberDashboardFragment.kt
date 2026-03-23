package com.example.apuniliapp.ui.membership

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.MemberBenefit
import com.example.apuniliapp.data.model.MemberProfile
import com.example.apuniliapp.databinding.FragmentMemberDashboardBinding
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment affichant le dashboard du membre accepté
 * Affiche les informations de profil, les bénéfices, et l'accès rapide aux contenus
 */
class MemberDashboardFragment : Fragment() {

    private var _binding: FragmentMemberDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private val viewModel: MemberDashboardViewModel by viewModels()
    
    // Pour gérer les animations
    private val animationDuration = 300L
    private val staggerDelay = 50L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val currentUser = sessionManager.getLoggedInUser()

        if (currentUser == null) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = getString(R.string.error_user_not_authenticated)
            return
        }

        // Charger le profil et les bénéfices
        setupUI()
        observeViewModel()
        animateEntrance()

        // Charger les données
        viewModel.loadMemberProfile(currentUser.id)
        viewModel.loadMemberBenefits()
        viewModel.loadMembershipHistory(currentUser.id)
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir le profil quand on revient de l'édition
        if (_binding != null && isAdded) {
            val currentUser = sessionManager.getLoggedInUser()
            if (currentUser != null) {
                viewModel.loadMemberProfile(currentUser.id)
            }
        }
    }

    private fun setupUI() {
        val currentUser = sessionManager.getLoggedInUser()

        // Click sur la carte héros pour naviguer vers le profil
        binding.cardHeroProfile.setOnClickListener {
            try {
                if (currentUser != null) {
                    findNavController().navigate(R.id.action_member_dashboard_to_member_profile_edit)
                } else {
                    Snackbar.make(binding.root, R.string.error_user_not_authenticated, Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMsg = getString(R.string.error_access_profile, e.message ?: "")
                Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Bouton Certificat
        binding.btnDownloadCertificate.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_under_development, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        viewModel.memberProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                displayMemberProfile(profile)
                animateCardEntrance(binding.cardHeroProfile)
            }
        }

        viewModel.memberBenefits.observe(viewLifecycleOwner) { benefits ->
            displayBenefits(benefits)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = error
            }
        }
    }

    /**
     * Animation d'entrée pour l'ensemble du dashboard
     */
    private fun animateEntrance() {
        val fadeIn = ObjectAnimator.ofFloat(binding.root, View.ALPHA, 0f, 1f).apply {
            duration = animationDuration
        }
        fadeIn.start()
    }

    /**
     * Animation d'entrée pour une carte
     */
    private fun animateCardEntrance(card: View) {
        val fadeIn = ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f).apply {
            duration = 250
        }
        val scaleX = ObjectAnimator.ofFloat(card, View.SCALE_X, 0.95f, 1f).apply {
            duration = 250
        }
        val scaleY = ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.95f, 1f).apply {
            duration = 250
        }
        
        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            start()
        }
    }

    private fun displayMemberProfile(profile: MemberProfile) {
        // Nom du membre
        binding.tvMemberName.text = getString(R.string.member_name_format, profile.firstname, profile.lastname)

        // Email
        binding.tvMemberEmail.text = profile.email

        // Date d'adhésion
        val datePattern = getString(R.string.date_format)
        val dateFormat = SimpleDateFormat(datePattern, Locale.FRANCE)
        val memberSinceText = if (profile.membershipApprovalDate > 0) {
            getString(R.string.member_since_format, dateFormat.format(Date(profile.membershipApprovalDate)))
        } else {
            getString(R.string.member_status_active)
        }
        binding.tvMemberSince.text = memberSinceText

        // Statut
        binding.chipStatus.text = getString(R.string.member_status_active_label)
        binding.chipStatus.setChipBackgroundColorResource(R.color.status_active)

        // Photo du profil
        if (profile.photoUrl.isNotBlank()) {
            try {
                binding.ivProfilePhoto.setImageResource(R.drawable.ic_person)
            } catch (ignored: Exception) {
                binding.ivProfilePhoto.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun displayBenefits(benefits: List<MemberBenefit>) {
        binding.gridBenefits.removeAllViews()

        benefits.forEachIndexed { index, benefit ->
            val benefitCard = createBenefitCard(benefit)
            binding.gridBenefits.addView(benefitCard)
            
            // Animer l'entrée avec délai
            benefitCard.alpha = 0f
            benefitCard.postDelayed({
                animateCardEntrance(benefitCard)
            }, (index * staggerDelay))
        }
    }

    private fun createBenefitCard(benefit: MemberBenefit): MaterialCardView {
        val benefitColor = getBenefitColor(benefit.icon.name)
        val benefitBgColor = getBenefitBgColor(benefit.icon.name)
        
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(6, 6, 6, 6)
            }
            cardElevation = 1f
            radius = 12f
            setCardBackgroundColor(benefitBgColor)
        }

        val container = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
        }

        // Icône avec background
        val iconContainer = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                bottomMargin = 8
            }
            setBackgroundColor(benefitColor and 0x33FFFFFF or (benefitColor and 0xFF000000.toInt()))
        }

        val iconView = ImageView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setImageResource(getBenefitIcon(benefit.icon.name))
            setColorFilter(benefitColor)
            contentDescription = benefit.benefitName
        }
        iconContainer.addView(iconView)

        // Titre
        val titleView = MaterialTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 3
            }
            text = benefit.benefitName
            textSize = 13f
            setTextAppearance(R.style.DashboardBenefitTitle)
        }

        // Description
        val descView = MaterialTextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = benefit.description
            textSize = 10f
            setTextColor(requireContext().getColor(android.R.color.darker_gray))
        }

        container.addView(iconContainer)
        container.addView(titleView)
        container.addView(descView)
        card.addView(container)

        return card
    }

    private fun getBenefitIcon(iconName: String): Int {
        return when (iconName) {
            "GALLERY" -> R.drawable.ic_photo_library
            "DOCUMENT" -> R.drawable.ic_document
            "EVENT" -> R.drawable.ic_event
            "CERTIFICATE" -> R.drawable.ic_members
            "NETWORK" -> R.drawable.ic_people
            "PRIORITY" -> R.drawable.ic_star
            else -> R.drawable.ic_info
        }
    }

    private fun getBenefitColor(iconName: String): Int {
        return when (iconName) {
            "GALLERY" -> requireContext().getColor(R.color.benefit_gallery)
            "DOCUMENT" -> requireContext().getColor(R.color.benefit_documents)
            "EVENT" -> requireContext().getColor(R.color.benefit_events)
            "CERTIFICATE" -> requireContext().getColor(R.color.benefit_certificate)
            "NETWORK" -> requireContext().getColor(R.color.benefit_profile)
            "PRIORITY" -> requireContext().getColor(R.color.benefit_history)
            else -> requireContext().getColor(android.R.color.darker_gray)
        }
    }

    private fun getBenefitBgColor(iconName: String): Int {
        return when (iconName) {
            "GALLERY" -> requireContext().getColor(R.color.benefit_gallery_bg)
            "DOCUMENT" -> requireContext().getColor(R.color.benefit_documents_bg)
            "EVENT" -> requireContext().getColor(R.color.benefit_events_bg)
            "CERTIFICATE" -> requireContext().getColor(R.color.benefit_certificate_bg)
            "NETWORK" -> requireContext().getColor(R.color.benefit_profile_bg)
            "PRIORITY" -> requireContext().getColor(R.color.benefit_history_bg)
            else -> requireContext().getColor(android.R.color.white)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
