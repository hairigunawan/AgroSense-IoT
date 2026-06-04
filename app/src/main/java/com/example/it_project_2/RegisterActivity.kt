package com.example.it_project_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: Button

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                setLoading(false)
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Pendaftaran Google gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            setLoading(false)
            Log.w(TAG, "Google sign in canceled or failed. Result code: ${result.resultCode}")
            Toast.makeText(this, "Daftar Google dibatalkan/gagal (Kode: ${result.resultCode})", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = android.view.View.VISIBLE
            btnRegister.isEnabled = false
            btnGoogle.isEnabled = false
        } else {
            progressBar.visibility = android.view.View.GONE
            btnRegister.isEnabled = true
            btnGoogle.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Sesuaikan dengan layout Anda

        auth = Firebase.auth
        progressBar = findViewById(R.id.pb_register)
        btnRegister = findViewById(R.id.btn_register)
        btnGoogle = findViewById(R.id.btn_google_register)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Contoh: Ambil input dari EditText dan tombol
        val etName = findViewById<EditText>(R.id.et_name_register) // ID EditText nama
        val etEmail = findViewById<EditText>(R.id.et_email_register) // ID EditText email
        val etPassword = findViewById<EditText>(R.id.et_password_register) // ID EditText password

        btnGoogle.setOnClickListener {
            setLoading(true)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nama, Email dan password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                            
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                            user.sendEmailVerification().addOnCompleteListener { verificationTask ->
                                setLoading(false)
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(baseContext, "Registrasi Berhasil. Silakan cek email Anda untuk verifikasi.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(baseContext, "Gagal mengirim email verifikasi: ${verificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                                
                                auth.signOut()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        // Registrasi gagal
                        setLoading(false)
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

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithGoogle:success")
                    Toast.makeText(this, "Pendaftaran Google Berhasil.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "signInWithGoogle:failure", task.exception)
                    Toast.makeText(this, "Pendaftaran Google Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}