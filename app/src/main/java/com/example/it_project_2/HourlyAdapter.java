package com.example.it_project_2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private List<HourlyWeather> hourlyWeatherList;

    public HourlyAdapter(List<HourlyWeather> hourlyWeatherList) {
        this.hourlyWeatherList = hourlyWeatherList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hour, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyWeather weather = hourlyWeatherList.get(position);
        holder.tvHour.setText(weather.getTime());
        holder.tvHourTemp.setText(weather.getTemp());

        if (weather.getIconUrl() != null && !weather.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(weather.getIconUrl())
                    .into(holder.ivHourIcon);
        } else {
            holder.ivHourIcon.setImageResource(weather.getIconRes());
        }
    }

    @Override
    public int getItemCount() {
        return hourlyWeatherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour, tvHourTemp;
        ImageView ivHourIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            tvHourTemp = itemView.findViewById(R.id.tvHourTemp);
            ivHourIcon = itemView.findViewById(R.id.ivHourIcon);
        }
    }
}
