package com.example.apuniliapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apuniliapp.databinding.ItemValueCardBinding

data class ValueItem(
    val iconRes: Int,
    val title: String,
    val description: String
)

class ValueAdapter(
    private val items: List<ValueItem>
) : RecyclerView.Adapter<ValueAdapter.ValueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValueViewHolder {
        val binding = ItemValueCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ValueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ValueViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ValueViewHolder(
        private val binding: ItemValueCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ValueItem) {
            binding.ivValueIcon.setImageResource(item.iconRes)
            binding.tvValueTitle.text = item.title
            binding.tvValueDescription.text = item.description
        }
    }
}

