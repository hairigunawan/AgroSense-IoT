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
            isNight || hour < 5 || hour > 18 -> "night"
            hour in 5..11 -> "morning"
            hour in 11..15 -> "day"
            else -> "afternoon" // 16..17
        }

        return when (conditionCode) {
            // Clear
            1000 -> {
                when (timePhase) {
                    "morning" -> WeatherTheme(R.drawable.mode_pagi, R.drawable.cerah, false)
                    "afternoon" -> WeatherTheme(R.drawable.mode_sore, R.drawable.cerah, false)
                    "night" -> WeatherTheme(R.drawable.mode_malam, R.drawable.cuaca_malam_cerah, true)
                    else -> WeatherTheme(R.drawable.mode_siang, R.drawable.cerah, false) // day
                }
            }
            // Cloudy/Mist/Fog
            1003, 1006, 1009, 1030, 1135, 1147 -> {
                when (timePhase) {
                    "morning" -> WeatherTheme(R.drawable.mode_pagi, R.drawable.cerah, false)
                    "afternoon" -> WeatherTheme(R.drawable.mode_sore, R.drawable.cerah, false)
                    "night" -> WeatherTheme(R.drawable.mode_malam, R.drawable.cuaca_malam_cerah, true)
                    else -> WeatherTheme(R.drawable.mode_siang, R.drawable.cerah, false) // day
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
            else -> WeatherTheme(R.drawable.mode_siang, R.drawable.cerah, false)
        }
    }

    fun translateCondition(condition: String): String {
        return when (condition.lowercase().trim()) {
            "patchy rain nearby", "patchy rain possible" -> "Hujan ringan di sekitar"
            "patchy light drizzle" -> "Gerimis ringan di sekitar"
            "light drizzle" -> "Gerimis ringan"
            "freezing drizzle" -> "Gerimis beku"
            "heavy freezing drizzle" -> "Gerimis beku lebat"
            "patchy light rain" -> "Hujan ringan"
            "light rain" -> "Hujan ringan"
            "moderate rain at times" -> "Hujan sedang sesekali"
            "moderate rain" -> "Hujan sedang"
            "heavy rain at times" -> "Hujan lebat sesekali"
            "heavy rain" -> "Hujan lebat"
            "light freezing rain" -> "Hujan beku ringan"
            "moderate or heavy freezing rain" -> "Hujan beku sedang atau lebat"
            "light sleet" -> "Hujan es ringan"
            "moderate or heavy sleet" -> "Hujan es sedang atau lebat"
            "patchy light snow" -> "Salju ringan"
            "light snow" -> "Salju ringan"
            "patchy moderate snow" -> "Salju sedang"
            "moderate snow" -> "Salju sedang"
            "patchy heavy snow" -> "Salju lebat"
            "heavy snow" -> "Salju lebat"
            "ice pellets" -> "Hujan es"
            "light rain shower" -> "Hujan ringan sesekali"
            "moderate or heavy rain shower" -> "Hujan sedang atau lebat sesekali"
            "torrential rain shower" -> "Hujan deras sesekali"
            "light sleet showers" -> "Hujan es ringan sesekali"
            "moderate or heavy sleet showers" -> "Hujan es sedang atau lebat sesekali"
            "light snow showers" -> "Salju ringan sesekali"
            "moderate or heavy snow showers" -> "Salju sedang atau lebat sesekali"
            "light showers of ice pellets" -> "Hujan es ringan sesekali"
            "moderate or heavy showers of ice pellets" -> "Hujan es sedang atau lebat sesekali"
            "patchy light rain with thunder" -> "Hujan ringan dan petir di sekitar"
            "moderate or heavy rain with thunder" -> "Hujan lebat dan petir"
            "patchy light snow with thunder" -> "Salju ringan dan petir di sekitar"
            "moderate or heavy snow with thunder" -> "Salju lebat dan petir"
            "sunny", "clear" -> "Cerah"
            "partly cloudy" -> "Cerah berawan"
            "cloudy" -> "Berawan"
            "overcast" -> "Mendung"
            "mist" -> "Berkabut"
            "thundery outbreaks possible" -> "Potensi badai petir"
            "blowing snow" -> "Salju tertiup angin"
            "blizzard" -> "Badai salju"
            "fog" -> "Kabut"
            "freezing fog" -> "Kabut beku"
            else -> condition
        }
    }
}
