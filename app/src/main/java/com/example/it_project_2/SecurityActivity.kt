package com.example.it_project_2

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.it_project_2.databinding.ActivitySecurityBinding
import com.example.it_project_2.security.BiometricCredentialStore
import java.util.concurrent.Executor

import com.google.firebase.auth.FirebaseAuth

class SecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecurityBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val auth = FirebaseAuth.getInstance()

    // Flag to prevent triggering listener when we change it programmatically
    private var isProgrammaticChange = false

    private fun getEncryptedSharedPreferences(): androidx.security.crypto.EncryptedSharedPreferences {
        val masterKey = androidx.security.crypto.MasterKey.Builder(this)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            androidx.security.crypto.EncryptedSharedPreferences.create(
                this,
                "app_settings_encrypted",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as androidx.security.crypto.EncryptedSharedPreferences
        } catch (e: Exception) {
            // If decryption fails, try deleting the preferences file and recreating it
            val fileName = "app_settings_encrypted"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                this.deleteSharedPreferences(fileName)
            } else {
                val file = java.io.File(this.filesDir.parent + "/shared_prefs/" + fileName + ".xml")
                if (file.exists()) file.delete()
            }
            
            // Re-try creation
            androidx.security.crypto.EncryptedSharedPreferences.create(
                this,
                fileName,
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as androidx.security.crypto.EncryptedSharedPreferences
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupBiometric()
    }

    private fun setupListeners() {
        val sharedPreferences = getEncryptedSharedPreferences()
        updateBiometricInfoText()

        // Load current state
        val isBiometricEnabled = sharedPreferences.getBoolean("is_biometric_enabled", false) &&
            hasSavedBiometricCredentialsForCurrentUser()

        if (sharedPreferences.getBoolean("is_biometric_enabled", false) && !hasSavedBiometricCredentialsForCurrentUser()) {
            sharedPreferences.edit().putBoolean("is_biometric_enabled", false).apply()
        }

        isProgrammaticChange = true
        binding.switchFingerprint.isChecked = isBiometricEnabled
        isProgrammaticChange = false

        binding.btnBackSecurity.setOnClickListener {
            finish()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnVerifyEmail.setOnClickListener {
            sendVerificationEmail()
        }
    }

    private fun showChangePasswordDialog() {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Ubah Password")
            builder.setMessage("Kami akan mengirimkan tautan reset password ke email Anda (${user.email}). Anda akan otomatis logout setelah email dikirim untuk alasan keamanan.")
            
            builder.setPositiveButton("Kirim") { _, _ ->
                auth.sendPasswordResetEmail(user.email!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email reset password telah dikirim!", Toast.LENGTH_LONG).show()
                        // Logout user to force password change
                        auth.signOut()
                        val intent = android.content.Intent(this, LoginActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal mengirim email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            builder.setNegativeButton("Batal", null)
            builder.show()
        }
    }

    private fun sendVerificationEmail() {
        val user = auth.currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                Toast.makeText(this, "Email Anda sudah terverifikasi!", Toast.LENGTH_SHORT).show()
            } else {
                user.sendEmailVerification().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email verifikasi telah dikirim ke ${user.email}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Gagal mengirim email verifikasi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        val sharedPreferences = getEncryptedSharedPreferences()

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Autentikasi dibatalkan/error: $errString", Toast.LENGTH_SHORT).show()
                    revertSwitchToFalse()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Kunci Sidik Jari berhasil diaktifkan!", Toast.LENGTH_SHORT).show()
                    sharedPreferences.edit().putBoolean("is_biometric_enabled", true).apply()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autentikasi gagal, coba lagi", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikasi Sidik Jari")
            .setSubtitle("Pindai sidik jari Anda untuk mengaktifkan fitur ini")
            .setNegativeButtonText("Batal")
            .build()

        binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            if (isProgrammaticChange) return@setOnCheckedChangeListener

            if (isChecked) {
                if (!hasSavedBiometricCredentialsForCurrentUser()) {
                    Toast.makeText(
                        this,
                        "Login sidik jari hanya bisa dipakai setelah login dengan email dan password pada akun ini.",
                        Toast.LENGTH_LONG
                    ).show()
                    revertSwitchToFalse()
                    return@setOnCheckedChangeListener
                }

                // User wants to enable it -> Require authentication
                val biometricManager = BiometricManager.from(this)
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        biometricPrompt.authenticate(promptInfo)
                    }
                    else -> {
                        Toast.makeText(this, "Fitur sidik jari tidak tersedia di perangkat ini", Toast.LENGTH_LONG).show()
                        revertSwitchToFalse()
                    }
                }
            } else {
                // User wants to disable it -> Just disable
                sharedPreferences.edit().putBoolean("is_biometric_enabled", false).apply()
            }
        }
    }

    private fun revertSwitchToFalse() {
        isProgrammaticChange = true
        binding.switchFingerprint.isChecked = false
        isProgrammaticChange = false
    }

    private fun updateBiometricInfoText() {
        val tvFingerprintSubtitle = findViewById<TextView>(R.id.tv_fingerprint_subtitle)
        val tvFingerprintNote = findViewById<TextView>(R.id.tv_fingerprint_note)
        val hasCredentials = hasSavedBiometricCredentialsForCurrentUser()
        val isGoogleAccount = auth.currentUser
            ?.providerData
            ?.any { it.providerId == "google.com" } == true

        when {
            hasCredentials -> {
                tvFingerprintSubtitle.text = "Gunakan biometrik untuk masuk"
                tvFingerprintNote.text = "Siap dipakai untuk login biometrik pada perangkat ini."
            }
            isGoogleAccount -> {
                tvFingerprintSubtitle.text = "Belum tersedia untuk akun Google"
                tvFingerprintNote.text = "Login sidik jari saat ini hanya tersedia setelah login dengan email dan password pada perangkat ini."
            }
            else -> {
                tvFingerprintSubtitle.text = "Perlu login email dan password"
                tvFingerprintNote.text = "Aktifkan fitur ini setelah Anda login ulang dengan email dan password pada akun ini."
            }
        }
    }

    private fun hasSavedBiometricCredentialsForCurrentUser(): Boolean {
        return BiometricCredentialStore.hasPasswordCredentialsForEmail(this, auth.currentUser?.email)
    }
}
