package com.example.it_project_2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.it_project_2.R
import com.example.it_project_2.databinding.ItemNotificationBinding
import com.example.it_project_2.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(private val notificationList: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.binding.tvTitle.text = notification.title
        holder.binding.tvMessage.text = notification.message
        
        // Format Timestamp
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        holder.binding.tvTime.text = sdf.format(Date(notification.timestamp))

        // Set Icon and colors based on type
        when (notification.type.lowercase()) {
            "success" -> {
                holder.binding.ivIcon.setImageResource(R.drawable.ic_rain)
                holder.binding.ivIcon.setColorFilter(Color.parseColor("#4CAF50"))
                holder.binding.ivIcon.setBackgroundResource(R.drawable.bg_icon_circle)
            }
            "warning", "danger" -> {
                holder.binding.ivIcon.setImageResource(R.drawable.ic_water_drop)
                holder.binding.ivIcon.setColorFilter(Color.parseColor("#E53935"))
                holder.binding.ivIcon.setBackgroundResource(R.drawable.bg_badge_red)
            }
            else -> {
                holder.binding.ivIcon.setImageResource(R.drawable.notification)
                holder.binding.ivIcon.setColorFilter(Color.parseColor("#2196F3"))
                holder.binding.ivIcon.setBackgroundResource(R.drawable.bg_icon_circle)
            }
        }
    }

    override fun getItemCount(): Int = notificationList.size
}
