package com.example.it_project_2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private TextView tvSuhu, tvKelembapan, tvHomeWeatherSummary, tvStatusPenyiraman;
    private View viewStatusDot;
    private FusedLocationProviderClient fusedLocationClient;
    private final String WEATHER_API_KEY = "440b67ccfc304047819164625260205";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHomeWeatherSummary = view.findViewById(R.id.tvHomeWeatherSummary);

        // Inisialisasi icon profile dan navigasi ke ProfileFragment
        ImageView ivProfile = view.findViewById(R.id.iv_profile_home);
        
        // Load profile picture from Firebase Auth using Glide
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                     .load(photoUrl)
                     .placeholder(R.drawable.ic_person)
                     .error(R.drawable.ic_person)
                     .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_person);
            }
        }

        ivProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView navView = 
                        getActivity().findViewById(R.id.bottom_navigation);
                if (navView != null) {
                    navView.setSelectedItemId(R.id.navigation_profile);
                }
            }
        });

        // Navigasi ke WeatherActivityModern jika card cuaca diklik
        View cardWeather = view.findViewById(R.id.cardWeather);
        if (cardWeather != null) {
            cardWeather.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), WeatherActivityModern.class);
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
        tvStatusPenyiraman = view.findViewById(R.id.tvStatusPenyiraman);
        viewStatusDot = view.findViewById(R.id.viewStatusDot);
        TextView tvOnlineStatus = view.findViewById(R.id.tvOnlineStatus);

        // Pulse animation for Online Status
        if (tvOnlineStatus != null) {
            android.animation.ObjectAnimator pulseAnim = android.animation.ObjectAnimator.ofFloat(tvOnlineStatus, "alpha", 1f, 0.4f);
            pulseAnim.setDuration(1200);
            pulseAnim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            pulseAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            pulseAnim.start();
        }

        // Animator for water pump status
        android.animation.ObjectAnimator pumpAnim = android.animation.ObjectAnimator.ofFloat(viewStatusDot, "scaleX", 1f, 1.4f);
        pumpAnim.setDuration(800);
        pumpAnim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        pumpAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        
        android.animation.ObjectAnimator pumpAnimY = android.animation.ObjectAnimator.ofFloat(viewStatusDot, "scaleY", 1f, 1.4f);
        pumpAnimY.setDuration(800);
        pumpAnimY.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        pumpAnimY.setRepeatCount(android.animation.ValueAnimator.INFINITE);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("sensor");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Integer suhu = snapshot.child("suhu").getValue(Integer.class);
                Integer kelembapan = snapshot.child("kelembapan").getValue(Integer.class);
                Boolean pompa = snapshot.child("pompa").getValue(Boolean.class);

                if (suhu != null && tvSuhu != null) {
                    tvSuhu.setText(suhu + "°C");
                }

                if (kelembapan != null && tvKelembapan != null) {
                    tvKelembapan.setText(kelembapan + "%");
                }

                if (pompa != null && tvStatusPenyiraman != null && viewStatusDot != null) {
                    if (pompa) {
                        tvStatusPenyiraman.setText("Melakukan penyiraman");
                        tvStatusPenyiraman.setTextColor(getResources().getColor(R.color.accent_blue));
                        viewStatusDot.setBackgroundResource(R.drawable.dot_green);
                        // start pulse animation
                        if (!pumpAnim.isRunning()) {
                            pumpAnim.start();
                            pumpAnimY.start();
                        }
                    } else {
                        tvStatusPenyiraman.setText("Tidak melakukan penyiraman");
                        tvStatusPenyiraman.setTextColor(getResources().getColor(R.color.text_dark));
                        viewStatusDot.setBackgroundResource(android.R.color.darker_gray);
                        // stop pulse animation
                        pumpAnim.cancel();
                        pumpAnimY.cancel();
                        viewStatusDot.setScaleX(1f);
                        viewStatusDot.setScaleY(1f);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        });
    }
}
