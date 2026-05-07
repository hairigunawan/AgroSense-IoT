package com.example.it_project_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth





class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Sesuaikan dengan layout Anda

        // tombol back
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish() // balik ke login
        }

        auth = Firebase.auth

        // Contoh: Ambil input dari EditText dan tombol
        val etName = findViewById<EditText>(R.id.et_name_register) // ID EditText nama
        val etEmail = findViewById<EditText>(R.id.et_email_register) // ID EditText email
        val etPassword = findViewById<EditText>(R.id.et_password_register) // ID EditText password
        val btnRegister = findViewById<Button>(R.id.btn_register) // ID Button daftar

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            // Client-side validation:
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nama, Email dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) { // Firebase Auth default min 6 karakter
                Toast.makeText(this, "Password minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Tambahkan validasi lain (misal: kombinasi huruf+angka) jika diperlukan

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registrasi berhasil
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        
                        // Set Display Name
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user?.updateProfile(profileUpdates)
                        
                        // Kirim email verifikasi
                        user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Toast.makeText(baseContext, "Registrasi Berhasil. Silakan cek email Anda untuk verifikasi.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(baseContext, "Gagal mengirim email verifikasi: ${verificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                            
                            // Logout user setelah registrasi karena belum verifikasi email
                            auth.signOut()
                            
                            // Redirect ke LoginActivity
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        // Registrasi gagal
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Registrasi Gagal: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
        
        // Optional: tambahkan listener untuk tombol masuk
        val tvGoToLogin = findViewById<TextView>(R.id.tv_go_to_login)
        tvGoToLogin?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}