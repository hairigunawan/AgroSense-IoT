package com.example.it_project_2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    // WeatherAPI.com endpoint: forecast.json?key={key}&q={city}&days=1&aqi=no&alerts=no
    @GET("forecast.json")
    Call<WeatherApiResponse> getForecast(
        @Query("key") String apiKey,
        @Query("q") String city,
        @Query("days") int days,
        @Query("aqi") String aqi,
        @Query("alerts") String alerts
    );

    // Old method for OpenWeather (kept for backward compatibility if needed in HomeFragment)
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
        @Query("q") String city,
        @Query("appid") String apiKey,
        @Query("units") String units
    );
}
