package com.example.it_project_2

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AgroSenseApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Aktifkan Firebase Realtime Database offline persistence
        // Data akan di-cache lokal dan tetap bisa diakses saat offline
        // Sinkronisasi otomatis terjadi saat koneksi kembali
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
