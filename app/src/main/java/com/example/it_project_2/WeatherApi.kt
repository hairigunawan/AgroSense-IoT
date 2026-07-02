package com.example.it_project_2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    // WeatherAPI.com endpoint: forecast.json?key={key}&q={city}&days=1&aqi=no&alerts=no
    @GET("forecast.json")
    fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") city: String,
        @Query("days") days: Int,
        @Query("aqi") aqi: String,
        @Query("alerts") alerts: String,
        @Query("lang") lang: String
    ): Call<WeatherApiResponse>

    // Old method for OpenWeather (kept for backward compatibility if needed in HomeFragment)
    @GET("weather")
    fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<WeatherResponse>
}
