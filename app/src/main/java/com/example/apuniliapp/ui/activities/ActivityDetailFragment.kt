package com.example.apuniliapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.apuniliapp.R
import com.example.apuniliapp.data.repository.firebase.FirebaseActivityRepository
import com.example.apuniliapp.databinding.FragmentActivityDetailBinding
import kotlinx.coroutines.launch

class ActivityDetailFragment : Fragment() {

    private var _binding: FragmentActivityDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityId = arguments?.getString("activityId") ?: ""

        lifecycleScope.launch {
            val activity = FirebaseActivityRepository.getActivityById(activityId)

            if (activity != null && isAdded) {
                binding.apply {
                    tvDetailTitle.text = activity.title
                    tvDetailDate.text = activity.date
                    tvDetailLocation.text = activity.location
                    tvDetailDescription.text = activity.description

                    // Charger la première image
                    val firstPhoto = activity.photoUrls.firstOrNull()
                    if (!firstPhoto.isNullOrBlank()) {
                        detailImage.load(firstPhoto) {
                            crossfade(true)
                        }
                    }

                    if (activity.category.isNotBlank()) {
                        chipDetailCategory.text = activity.category
                        chipDetailCategory.isVisible = true
                    } else {
                        chipDetailCategory.isVisible = false
                    }

                    btnDetailShare.setOnClickListener {
                        val shareText = getString(
                            R.string.activities_share_text,
                            activity.title,
                            activity.description,
                            activity.date,
                            activity.location
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_SUBJECT, activity.title)
                        }
                        startActivity(Intent.createChooser(intent, getString(R.string.activities_share)))
                    }
                }
            }
        }

        animateEntrance()
    }

    private fun animateEntrance() {
        // Fade in the image area
        binding.detailImage.alpha = 0f
        binding.detailImage.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        // Slide up the content
        val contentViews = listOf(
            binding.tvDetailTitle,
            binding.tvDetailDate,
            binding.tvDetailLocation,
            binding.tvDetailDescription,
            binding.btnDetailShare
        )
        contentViews.forEachIndexed { index, v ->
            v.alpha = 0f
            v.translationY = 30f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((150 + index * 80).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
