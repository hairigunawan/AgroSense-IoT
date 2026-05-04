package com.example.it_project_2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity {

    private RecyclerView rvHourly;
    private HourlyAdapter hourlyAdapter;
    private List<HourlyWeather> hourlyWeatherList;

    private TextView tvLocation, tvMainTemp, tvWeatherDesc, tvHighLow, tvFeelsLike, tvLastUpdated;
    private final String WEATHER_API_KEY = "440b67ccfc304047819164625260205";
    
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

        // Initialize Views
        tvLocation = findViewById(R.id.tvLocation);
        tvMainTemp = findViewById(R.id.tvMainTemp);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvHighLow = findViewById(R.id.tvHighLow);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);

        // Initialize RecyclerView
        rvHourly = findViewById(R.id.rvHourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        hourlyWeatherList = new ArrayList<>();
        hourlyAdapter = new HourlyAdapter(hourlyWeatherList);
        rvHourly.setAdapter(hourlyAdapter);

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestFreshLocation();
        }
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        String query = location.getLatitude() + "," + location.getLongitude();
                        getWeatherForecast(query);
                    } else {
                        // Coba last location jika current location null
                        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location lastLoc) {
                                if (lastLoc != null) {
                                    getWeatherForecast(lastLoc.getLatitude() + "," + lastLoc.getLongitude());
                                } else {
                                    getWeatherForecast("Ujung Batu");
                                }
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestFreshLocation();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak, menggunakan lokasi default", Toast.LENGTH_SHORT).show();
                getWeatherForecast("Ujung Batu");
            }
        }
    }

    private void getWeatherForecast(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.weatherapi.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);
        Call<WeatherApiResponse> call = weatherApi.getForecast(WEATHER_API_KEY, query, 1, "no", "no");

        call.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(Call<WeatherApiResponse> call, Response<WeatherApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherApiResponse data = response.body();
                    updateUI(data);
                } else {
                    Toast.makeText(WeatherActivity.this, "Gagal mengambil data: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherApiResponse> call, Throwable t) {
                Toast.makeText(WeatherActivity.this, "Koneksi Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(WeatherApiResponse data) {
        tvLocation.setText(data.getLocation().getName());
        tvMainTemp.setText(Math.round(data.getCurrent().getTempC()) + "°");
        tvWeatherDesc.setText(data.getCurrent().getCondition().getText());
        tvFeelsLike.setText("Terasa seperti " + Math.round(data.getCurrent().getFeelsLikeC()) + "°");
        tvLastUpdated.setText("Terakhir diperbarui: " + data.getCurrent().getLastUpdated());

        if (data.getForecast() != null && !data.getForecast().getForecastDay().isEmpty()) {
            WeatherApiResponse.ForecastDay today = data.getForecast().getForecastDay().get(0);
            tvHighLow.setText("Maks: " + Math.round(today.getDay().getMaxTempC()) + "°  Min: " + Math.round(today.getDay().getMinTempC()) + "°");

            hourlyWeatherList.clear();
            List<WeatherApiResponse.Hour> hours = today.getHour();
            for (WeatherApiResponse.Hour hour : hours) {
                hourlyWeatherList.add(new HourlyWeather(
                        hour.getTime(),
                        hour.getCondition().getIcon(),
                        Math.round(hour.getTempC()) + "°"
                ));
            }
            hourlyAdapter.notifyDataSetChanged();
        }
    }
}
