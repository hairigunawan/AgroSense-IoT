package com.example.it_project_2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HourlyAdapter(private val hourlyWeatherList: List<HourlyWeather>) :
    RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hour, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weather = hourlyWeatherList[position]
        holder.tvHour.text = weather.time
        holder.tvHourTemp.text = weather.temp

        if (!weather.iconUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(weather.iconUrl)
                .into(holder.ivHourIcon)
        } else {
            holder.ivHourIcon.setImageResource(weather.iconRes)
        }
    }

    override fun getItemCount(): Int = hourlyWeatherList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHour: TextView = itemView.findViewById(R.id.tvHour)
        val tvHourTemp: TextView = itemView.findViewById(R.id.tvHourTemp)
        val ivHourIcon: ImageView = itemView.findViewById(R.id.ivHourIcon)
    }
}
