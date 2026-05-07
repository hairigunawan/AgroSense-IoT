package com.example.it_project_2;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity yang menangani navigasi fragment
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Aktifkan Edge-to-Edge agar layout mengisi seluruh layar (termasuk area status bar & navigasi)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Menangani Insets untuk Fragment Container (Status Bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Tambahkan padding atas agar konten fragment tidak tertutup status bar
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

        // Menangani Insets untuk Navbar Card (Navigation Bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation_card), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            // Margin bawah = tinggi navigasi sistem (tombol/gestur) + margin dasar (24dp)
            int marginBase = (int) (24 * getResources().getDisplayMetrics().density);
            mlp.bottomMargin = insets.bottom + marginBase;
            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });

        // Load fragment default (Home) jika pertama kali dibuka

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Listener untuk Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.navigation_settings) {
                fragment = new SettingsFragment();
            } else if (itemId == R.id.navigation_history) {
                fragment = new HistoryFragment();
            } else if (itemId == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    /**
     * Helper untuk mengganti fragment di fragment_container
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
