package com.example.apuniliapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.databinding.ItemHeroBannerBinding

data class BannerItem(
    val title: String,
    val subtitle: String,
    val backgroundRes: Int
)

class HeroBannerAdapter(
    private val items: List<BannerItem>
) : RecyclerView.Adapter<HeroBannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemHeroBannerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BannerViewHolder(
        private val binding: ItemHeroBannerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BannerItem) {
            // Seul le fond change entre les slides,
            // le texte est géré par le fragment overlay
            binding.bannerBackground.setBackgroundResource(item.backgroundRes)
        }
    }
}
