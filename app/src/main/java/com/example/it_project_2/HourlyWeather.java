package com.example.it_project_2;

public class HourlyWeather {
    private String time;
    private int iconRes;
    private String iconUrl;
    private String temp;

    public HourlyWeather(String time, int iconRes, String temp) {
        this.time = time;
        this.iconRes = iconRes;
        this.temp = temp;
    }

    public HourlyWeather(String time, String iconUrl, String temp) {
        this.time = time;
        this.iconUrl = iconUrl;
        this.temp = temp;
    }

    public String getTime() {
        return time;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getTemp() {
        return temp;
    }
}
