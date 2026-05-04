package com.example.it_project_2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fragment untuk halaman Beranda yang menampilkan data sensor dan cuaca real-time.
 */
public class HomeFragment extends Fragment {

    private TextView tvSuhu, tvKelembapan, tvHomeWeatherSummary;
    private FusedLocationProviderClient fusedLocationClient;
    private final String WEATHER_API_KEY = "440b67ccfc304047819164625260205";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHomeWeatherSummary = view.findViewById(R.id.tvHomeWeatherSummary);

        // Inisialisasi icon profile dan navigasi ke ProfileFragment
        ImageView ivProfile = view.findViewById(R.id.iv_profile_home);
        ivProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView navView = 
                        getActivity().findViewById(R.id.bottom_navigation);
                if (navView != null) {
                    navView.setSelectedItemId(R.id.navigation_profile);
                }
            }
        });

        // Navigasi ke WeatherActivity jika card cuaca diklik
        View cardWeather = view.findViewById(R.id.cardWeather);
        if (cardWeather != null) {
            cardWeather.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), WeatherActivity.class);
                startActivity(intent);
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fetchLocationAndWeather();

        return view;
    }

    private void fetchLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getWeatherSummary("Ujung Batu");
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
            .addOnSuccessListener(location -> {
                if (location != null) {
                    getWeatherSummary(location.getLatitude() + "," + location.getLongitude());
                } else {
                    getWeatherSummary("Ujung Batu");
                }
            });
    }

    private void getWeatherSummary(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.weatherapi.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);
        Call<WeatherApiResponse> call = weatherApi.getForecast(WEATHER_API_KEY, query, 1, "no", "no");

        call.enqueue(new Callback<WeatherApiResponse>() {
            @Override
            public void onResponse(Call<WeatherApiResponse> call, Response<WeatherApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    WeatherApiResponse data = response.body();
                    String summary = data.getLocation().getName() + ", " + Math.round(data.getCurrent().getTempC()) + "°C";
                    tvHomeWeatherSummary.setText(summary);
                }
            }

            @Override
            public void onFailure(Call<WeatherApiResponse> call, Throwable t) {
                // Log failure but don't show toast to user on home card
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSuhu = view.findViewById(R.id.suhuValue);
        tvKelembapan = view.findViewById(R.id.kelembapanValue);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("sensor");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer suhu = snapshot.child("suhu").getValue(Integer.class);
                Integer kelembapan = snapshot.child("kelembapan").getValue(Integer.class);

                if (suhu != null && tvSuhu != null) {
                    tvSuhu.setText(suhu + "°C");
                }

                if (kelembapan != null && tvKelembapan != null) {
                    tvKelembapan.setText(kelembapan + "%");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        });
    }
}
