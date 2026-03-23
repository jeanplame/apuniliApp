package com.example.apuniliapp.ui.home

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.apuniliapp.R
import com.example.apuniliapp.data.model.UserRole
import com.example.apuniliapp.databinding.FragmentHomeBinding
import com.example.apuniliapp.ui.adapter.ActivityAdapter
import com.example.apuniliapp.ui.adapter.EventAdapter
import com.example.apuniliapp.ui.adapter.HeroBannerAdapter
import com.example.apuniliapp.ui.adapter.ValueAdapter
import com.example.apuniliapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var eventAdapter: EventAdapter
    private var isPresidentWordExpanded = false

    // Hero carousel auto-slide
    private val autoSlideHandler = Handler(Looper.getMainLooper())
    private var autoSlideRunnable: Runnable? = null
    private var currentBannerCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupAdapters()
        setupClickListeners()
        setupSwipeRefresh()
        setupPresidentWord()
        observeViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateSectionsEntrance()
        updateVisibilityForRole()
    }

    override fun onResume() {
        super.onResume()
        startAutoSlide()
    }

    override fun onPause() {
        super.onPause()
        stopAutoSlide()
    }

    // ── Visibility par rôle ──────────────────────────────────────────

    private fun updateVisibilityForRole() {
        val sessionManager = SessionManager(requireContext())
        val role = sessionManager.getCurrentRole()

        when (role) {
            UserRole.VISITOR -> {
                binding.btnJoinUs.visibility = View.VISIBLE
                binding.btnCtaBecomeMember.visibility = View.VISIBLE
            }
            UserRole.MEMBER, UserRole.ADMIN -> {
                binding.btnJoinUs.visibility = View.GONE
                binding.btnCtaBecomeMember.visibility = View.GONE
            }
        }
    }

    // ── Adapters ─────────────────────────────────────────────────────

    private fun setupAdapters() {
        activityAdapter = ActivityAdapter { activity ->
            try {
                findNavController().navigate(
                    R.id.nav_activity_detail,
                    bundleOf("activityId" to activity.id)
                )
            } catch (_: Exception) { }
        }

        eventAdapter = EventAdapter { _ ->
            try {
                findNavController().navigate(R.id.nav_events)
            } catch (_: Exception) { }
        }

        binding.rvLatestActivities.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvLatestActivities.adapter = activityAdapter

        binding.rvUpcomingEvents.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvUpcomingEvents.adapter = eventAdapter

        // Values horizontal RecyclerView
        binding.rvValues.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    // ── Hero Carousel ────────────────────────────────────────────────

    private fun setupHeroCarousel(state: HomeUiState) {
        val bannerItems = state.bannerItems
        if (bannerItems.isEmpty()) return
        currentBannerCount = bannerItems.size

        val adapter = HeroBannerAdapter(bannerItems)
        binding.heroPager.adapter = adapter
        binding.heroPager.offscreenPageLimit = 1

        // Update hero text on page change
        binding.heroPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDotsIndicator(position)
                val item = bannerItems[position]
                binding.tvHeroTitle.text = item.title
                binding.tvHeroSubtitle.text = item.subtitle
            }
        })

        // Setup dots
        setupDotsIndicator(bannerItems.size)

        // Start auto-slide
        startAutoSlide()
    }

    private fun setupDotsIndicator(count: Int) {
        binding.dotsIndicator.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(requireContext()).apply {
                setImageResource(R.drawable.dot_indicator_selector)
                isSelected = (i == 0)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = if (i == 0) 0 else 8
                }
                layoutParams = params
            }
            binding.dotsIndicator.addView(dot)
        }
    }

    private fun updateDotsIndicator(selectedPosition: Int) {
        for (i in 0 until binding.dotsIndicator.childCount) {
            binding.dotsIndicator.getChildAt(i).isSelected = (i == selectedPosition)
        }
    }

    private fun startAutoSlide() {
        stopAutoSlide()
        if (currentBannerCount <= 1) return
        autoSlideRunnable = object : Runnable {
            override fun run() {
                if (_binding == null) return
                val currentItem = binding.heroPager.currentItem
                val nextItem = (currentItem + 1) % currentBannerCount
                binding.heroPager.setCurrentItem(nextItem, true)
                autoSlideHandler.postDelayed(this, 4000)
            }
        }
        autoSlideHandler.postDelayed(autoSlideRunnable!!, 4000)
    }

    private fun stopAutoSlide() {
        autoSlideRunnable?.let { autoSlideHandler.removeCallbacks(it) }
    }

    // ── Click Listeners ──────────────────────────────────────────────

    private fun setupClickListeners() {
        val sessionManager = SessionManager(requireContext())

        binding.btnJoinUs.setOnClickListener {
            navigateToMembership(sessionManager)
        }
        binding.btnContactUs.setOnClickListener {
            try { findNavController().navigate(R.id.nav_contact) } catch (_: Exception) { }
        }
        binding.btnSeeAllActivities.setOnClickListener {
            try { findNavController().navigate(R.id.nav_activities) } catch (_: Exception) { }
        }
        binding.btnSeeAllEvents.setOnClickListener {
            try { findNavController().navigate(R.id.nav_events) } catch (_: Exception) { }
        }
        binding.quickActivities.setOnClickListener {
            try { findNavController().navigate(R.id.nav_activities) } catch (_: Exception) { }
        }
        binding.quickEvents.setOnClickListener {
            try { findNavController().navigate(R.id.nav_events) } catch (_: Exception) { }
        }
        binding.quickGallery.setOnClickListener {
            try { findNavController().navigate(R.id.nav_gallery) } catch (_: Exception) { }
        }
        binding.quickDocuments.setOnClickListener {
            try { findNavController().navigate(R.id.nav_documents) } catch (_: Exception) { }
        }
        binding.btnCtaBecomeMember.setOnClickListener {
            navigateToMembership(sessionManager)
        }
        binding.btnCtaContact.setOnClickListener {
            try { findNavController().navigate(R.id.nav_contact) } catch (_: Exception) { }
        }
        binding.btnFacebook.setOnClickListener { openUrl("https://www.facebook.com/apunili") }
        binding.btnWhatsapp.setOnClickListener { openUrl("https://wa.me/243812345678") }
        binding.btnEmail.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:contact@apunili.org")
                    putExtra(Intent.EXTRA_SUBJECT, "Contact depuis l'application Apunili")
                }
                startActivity(Intent.createChooser(intent, "Envoyer un email"))
            } catch (_: Exception) { }
        }
        binding.btnPhone.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:+243812345678")
                }
                startActivity(intent)
            } catch (_: Exception) { }
        }
    }

    private fun navigateToMembership(sessionManager: SessionManager) {
        when (sessionManager.getCurrentRole()) {
            UserRole.VISITOR -> {
                Snackbar.make(binding.root, getString(R.string.membership_login_required_message), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.nav_login_action)) {
                        try { findNavController().navigate(R.id.nav_login) } catch (_: Exception) { }
                    }
                    .show()
            }
            UserRole.MEMBER -> {
                try { findNavController().navigate(R.id.nav_membership) } catch (_: Exception) { }
            }
            UserRole.ADMIN -> {
                try { findNavController().navigate(R.id.nav_admin_membership) } catch (_: Exception) { }
            }
        }
    }

    // ── President Word ───────────────────────────────────────────────

    private fun setupPresidentWord() {
        binding.btnReadMorePresident.setOnClickListener {
            isPresidentWordExpanded = !isPresidentWordExpanded
            binding.tvPresidentWord.maxLines = if (isPresidentWordExpanded) Int.MAX_VALUE else 3
            binding.btnReadMorePresident.text = getString(
                if (isPresidentWordExpanded) R.string.home_read_less else R.string.home_read_more
            )
            binding.btnReadMorePresident.setIconResource(
                if (isPresidentWordExpanded) R.drawable.ic_arrow_back else R.drawable.ic_arrow_forward
            )
        }
    }

    // ── Swipe Refresh ────────────────────────────────────────────────

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.md_theme_light_primary,
            R.color.md_theme_light_secondary,
            R.color.md_theme_light_tertiary
        )
        binding.swipeRefresh.setOnRefreshListener {
            homeViewModel.refresh()
        }
    }

    // ── ViewModel Observer ───────────────────────────────────────────

    private fun observeViewModel() {
        homeViewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false

            // Hero carousel (setup once)
            if (binding.heroPager.adapter == null && state.bannerItems.isNotEmpty()) {
                setupHeroCarousel(state)
            }

            // Values
            if (state.values.isNotEmpty() && binding.rvValues.adapter == null) {
                binding.rvValues.adapter = ValueAdapter(state.values)
            }

            // Activities
            if (state.latestActivities.isNotEmpty()) {
                activityAdapter.submitList(state.latestActivities)
                binding.rvLatestActivities.visibility = View.VISIBLE
                binding.tvEmptyActivities.visibility = View.GONE
            } else {
                binding.rvLatestActivities.visibility = View.GONE
                binding.tvEmptyActivities.visibility = View.VISIBLE
            }

            // Events
            if (state.upcomingEvents.isNotEmpty()) {
                eventAdapter.submitList(state.upcomingEvents)
                binding.rvUpcomingEvents.visibility = View.VISIBLE
                binding.tvEmptyEvents.visibility = View.GONE
            } else {
                binding.rvUpcomingEvents.visibility = View.GONE
                binding.tvEmptyEvents.visibility = View.VISIBLE
            }

            // President word
            if (state.presidentWord.isNotEmpty()) {
                binding.tvPresidentWord.text = state.presidentWord
            }

            // Stat counters with animation (only once)
            if (!state.hasAnimatedCounters) {
                animateCounter(binding.tvStatMembersCount, state.membersCount)
                animateCounter(binding.tvStatActivitiesCount, state.activitiesCount)
                animateCounter(binding.tvStatEventsCount, state.eventsCount)
                homeViewModel.markCountersAnimated()
            } else {
                binding.tvStatMembersCount.text = state.membersCount.toString()
                binding.tvStatActivitiesCount.text = state.activitiesCount.toString()
                binding.tvStatEventsCount.text = state.eventsCount.toString()
            }
        }
    }

    // ── Animations ───────────────────────────────────────────────────

    private fun animateCounter(textView: TextView, targetValue: Int) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1200
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = (animation.animatedValue as Int).toString()
        }
        animator.start()
    }

    private fun animateSectionsEntrance() {
        val scrollView = binding.scrollView
        val nestedContent = scrollView.getChildAt(0) as? ViewGroup ?: return
        for (i in 0 until nestedContent.childCount) {
            val child = nestedContent.getChildAt(i)
            child.alpha = 0f
            child.translationY = 60f
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay((i * 80).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    // ── Utils ────────────────────────────────────────────────────────

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (_: Exception) { }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoSlide()
        _binding = null
    }
}
