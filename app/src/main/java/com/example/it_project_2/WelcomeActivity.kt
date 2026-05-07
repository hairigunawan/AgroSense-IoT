package com.example.it_project_2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.scottyab.rootbeer.RootBeer

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Root Detection
        val rootBeer = RootBeer(this)
        if (rootBeer.isRooted) {
            AlertDialog.Builder(this)
                .setTitle("Peringatan Keamanan")
                .setMessage("Perangkat ini terdeteksi telah di-root. Untuk alasan keamanan, aplikasi tidak dapat dijalankan pada perangkat yang di-root.")
                .setCancelable(false)
                .setPositiveButton("Tutup") { _, _ ->
                    finishAffinity()
                }
                .show()
            return
        }

        // Inisialisasi Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Auto Login: Jika user sudah login dan email terverifikasi, langsung ke MainActivity
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Ambil View dari Layout
        val btnDaftar = findViewById<Button>(R.id.btn_daftar_welcome)
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)

        // 1. Klik teks "Masuk" -> ke LoginActivity
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 2. Klik tombol "Mulai Sekarang" -> ke RegisterActivity
        btnDaftar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
