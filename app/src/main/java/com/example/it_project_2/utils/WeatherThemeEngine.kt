package com.example.it_project_2.utils

import com.example.it_project_2.R
import java.util.Calendar

data class WeatherTheme(
    val backgroundResId: Int,
    val iconResId: Int,
    val isDarkTheme: Boolean,
    val isRaining: Boolean = false,
    val isStorming: Boolean = false
)

object WeatherThemeEngine {

    fun getTheme(conditionCode: Int, isDayFromApi: Int? = null): WeatherTheme {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Determine time phase
        // If API provides isDay, we respect it for strict night/day, otherwise calculate phase
        val isNight = if (isDayFromApi != null) isDayFromApi == 0 else hour !in 6..17
        val timePhase = when {
            isNight -> "night"
            hour in 5..9 -> "morning"
            hour in 10..15 -> "day"
            else -> "afternoon" // 16..17
        }

        return when (conditionCode) {
            // Clear
            1000 -> {
                when (timePhase) {
                    "morning" -> WeatherTheme(R.drawable.theme_morning_clear, R.drawable.cerah, false)
                    "afternoon" -> WeatherTheme(R.drawable.theme_afternoon_clear, R.drawable.cerah, false) // sunset
                    "night" -> WeatherTheme(R.drawable.theme_night_clear, R.drawable.cuaca_malam_cerah, true)
                    else -> WeatherTheme(R.drawable.theme_day_clear, R.drawable.cerah, false) // day
                }
            }
            // Cloudy / Partly Cloudy / Overcast / Mist / Fog
            1003, 1006, 1009, 1030, 1135, 1147 -> {
                if (isNight) {
                    WeatherTheme(R.drawable.theme_night_clear, R.drawable.cuaca_malam_cerah, true) // Can use cloudy night icon if available
                } else {
                    WeatherTheme(R.drawable.theme_cloudy, R.drawable.cerah, false) // Light cloudy
                }
            }
            // Rain (Light to Heavy)
            1063, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243, 1246 -> {
                 WeatherTheme(R.drawable.theme_rain, R.drawable.hujan_ringan, true, isRaining = true)
            }
            // Thunderstorm
            1087, 1273, 1276, 1279, 1282 -> {
                WeatherTheme(R.drawable.theme_storm, R.drawable.hujan_badai, true, isRaining = true, isStorming = true)
            }
            // Default to day clear
            else -> WeatherTheme(R.drawable.theme_day_clear, R.drawable.cerah, false)
        }
    }
}
