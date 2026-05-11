package com.example.it_project_2;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherApiResponse {
    @SerializedName("location")
    private Location location;

    @SerializedName("current")
    private Current current;

    @SerializedName("forecast")
    private Forecast forecast;

    public Location getLocation() { return location; }
    public Current getCurrent() { return current; }
    public Forecast getForecast() { return forecast; }

    public static class Location {
        @SerializedName("name")
        private String name;
        public String getName() { return name; }
    }

    public static class Current {
        @SerializedName("temp_c")
        private double tempC;

        @SerializedName("feelslike_c")
        private double feelsLikeC;

        @SerializedName("last_updated")
        private String lastUpdated;

        @SerializedName("condition")
        private Condition condition;

        @SerializedName("is_day")
        private int isDay;

        @SerializedName("humidity")
        private int humidity;

        public double getTempC() { return tempC; }
        public double getFeelsLikeC() { return feelsLikeC; }
        public String getLastUpdated() { return lastUpdated; }
        public Condition getCondition() { return condition; }
        public int getIsDay() { return isDay; }
        public int getHumidity() { return humidity; }
    }

    public static class Condition {
        @SerializedName("text")
        private String text;
        @SerializedName("icon")
        private String icon;

        public String getText() { return text; }
        public String getIcon() { return "https:" + icon; }
    }

    public static class Forecast {
        @SerializedName("forecastday")
        private List<ForecastDay> forecastDay;
        public List<ForecastDay> getForecastDay() { return forecastDay; }
    }

    public static class ForecastDay {
        @SerializedName("day")
        private Day day;
        @SerializedName("hour")
        private List<Hour> hour;
        public Day getDay() { return day; }
        public List<Hour> getHour() { return hour; }
    }

    public static class Day {
        @SerializedName("maxtemp_c")
        private double maxTempC;
        @SerializedName("mintemp_c")
        private double minTempC;
        public double getMaxTempC() { return maxTempC; }
        public double getMinTempC() { return minTempC; }
    }

    public static class Hour {
        @SerializedName("time")
        private String time;
        @SerializedName("temp_c")
        private double tempC;
        @SerializedName("condition")
        private Condition condition;

        public String getTime() {
            // Extract HH:mm from "yyyy-MM-dd HH:mm"
            if (time != null && time.length() > 11) {
                return time.substring(11);
            }
            return time;
        }
        public double getTempC() { return tempC; }
        public Condition getCondition() { return condition; }

        @SerializedName("chance_of_rain")
        private int chanceOfRain;
        public int getChanceOfRain() { return chanceOfRain; }
    }
}
