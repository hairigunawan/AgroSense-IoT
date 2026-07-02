package com.example.it_project_2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.it_project_2.databinding.ItemHourlyModernBinding

data class HourlyModern(
    val hour: String,
    val temp: String,
    val iconUrl: String,
    val rainChance: String
)

class HourlyModernAdapter(private val items: List<HourlyModern>) :
    RecyclerView.Adapter<HourlyModernAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHourlyModernBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyModernBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvHour.text = item.hour
        holder.binding.tvHourlyTemp.text = item.temp
        Glide.with(holder.itemView.context)
            .load(item.iconUrl)
            .into(holder.binding.ivWeatherIcon)
        holder.binding.tvRainChance.text = item.rainChance
    }

    override fun getItemCount() = items.size
}
