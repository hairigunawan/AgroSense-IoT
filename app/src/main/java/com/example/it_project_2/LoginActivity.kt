package com.example.it_project_2

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val etEmail = findViewById<EditText>(R.id.et_email_login)
        val etPassword = findViewById<EditText>(R.id.et_password_login)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnBiometric = findViewById<ImageButton>(R.id.btn_biometric)
        val tvRegister = findViewById<TextView>(R.id.tv_go_to_register)

        // Set Spannable text for tvRegister
        val fullText = "Belum punya akun? Daftar sekarang"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf("Daftar sekarang")
        if (startIndex != -1) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.green_button)),
                startIndex,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        tvRegister.text = spannable

        // Setup Biometric
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Autentikasi error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Autentikasi berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autentikasi gagal", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login Biometrik")
            .setSubtitle("Gunakan sidik jari Anda untuk login")
            .setNegativeButtonText("Gunakan Password")
            .build()

        btnBiometric.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    if (auth.currentUser != null) {
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        Toast.makeText(this, "Harap login dengan email & password terlebih dahulu", Toast.LENGTH_LONG).show()
                    }
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Toast.makeText(this, "Fitur biometrik tidak tersedia di perangkat ini", Toast.LENGTH_LONG).show()
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Toast.makeText(this, "Sensor biometrik sedang tidak tersedia", Toast.LENGTH_LONG).show()
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    Toast.makeText(this, "Belum ada sidik jari yang terdaftar", Toast.LENGTH_LONG).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user?.isEmailVerified == true) {
                            Log.d(TAG, "signInWithEmail:success")
                            Toast.makeText(baseContext, "Login Berhasil.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Log.w(TAG, "signInWithEmail:failure email not verified")
                            Toast.makeText(baseContext, "Harap verifikasi email Anda terlebih dahulu.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
