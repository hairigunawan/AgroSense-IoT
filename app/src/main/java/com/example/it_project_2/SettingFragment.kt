package com.example.it_project_2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.it_project_2.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            navigateToHome()
        }

        // Klik area profil untuk edit nama
        binding.cardProfileImage.setOnClickListener {
            showEditNameDialog()
        }

        // Navigasi ke halaman Notifikasi
        binding.btnNotification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

        // Navigasi ke halaman Keamanan
        binding.btnSecurity.setOnClickListener {
            val intent = Intent(requireContext(), SecurityActivity::class.java)
            startActivity(intent)
        }

        // Navigasi ke halaman Tentang Aplikasi
        binding.btnAbout.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            
            // Sign out from Google
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireContext(), gso)
            
            googleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun showEditNameDialog() {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Nama Pengguna")

        val input = EditText(context)
        input.hint = "Masukkan nama baru"
        input.setText(binding.tvProfileName.text)
        
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)
        
        builder.setView(container)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateUserName(newName)
            } else {
                Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updateUserName(newName: String) {
        val currentUser = auth.currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("users").document(currentUser.uid)
                        .update("nama", newName)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            binding.tvProfileName.text = newName
                        }
                        .addOnFailureListener {
                            binding.tvProfileName.text = newName
                        }
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            binding.tvProfileEmail.text = currentUser.email
            
            // Load foto profil dari URL (Google Auth)
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(binding.ivProfile)
            } else {
                binding.ivProfile.setImageResource(R.drawable.user)
            }

            val authName = currentUser.displayName
            if (!authName.isNullOrEmpty()) {
                binding.tvProfileName.text = authName
            } else {
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (_binding != null && document != null && document.exists()) {
                            val namaUser = document.getString("nama")
                            binding.tvProfileName.text = namaUser ?: "Pengguna"
                        }
                    }
            }
        }
    }

    private fun navigateToHome() {
        val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.navigation_home
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
