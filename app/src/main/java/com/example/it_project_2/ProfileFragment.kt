package com.example.it_project_2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.it_project_2.databinding.FragmentProfileBinding // Pastikan import ini sesuai dengan nama package kamu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    // Menggunakan ViewBinding untuk menghindari NullPointerException
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Launcher untuk memilih gambar dari Galeri
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                // Tampilkan gambar
                binding.ivProfile.setImageURI(it)
                
                // Simpan URI gambar ke SharedPreferences agar tidak hilang saat aplikasi ditutup
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                    with (sharedPref.edit()) {
                        putString("profile_image_${currentUser.uid}", it.toString())
                        apply()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout menggunakan ViewBinding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()

        // Setup tombol pilih gambar
        binding.cardProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            // Tambahkan flag agar izin baca persisten
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImageLauncher.launch(intent)
        }

        // Setup tombol logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            // Pastikan class LoginActivity sudah ada di project kamu
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Set Email langsung dari Firebase Auth
            binding.tvProfileEmail.text = currentUser.email

            // Prioritaskan Display Name dari Auth (karena disimpan saat registrasi)
            val authName = currentUser.displayName
            if (!authName.isNullOrEmpty()) {
                if (_binding != null) {
                    binding.tvProfileName.text = authName
                }
            } else {
                // Ambil Nama dari Firestore berdasarkan UID jika di Auth kosong
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (_binding != null && document != null && document.exists()) {
                            val namaUser = document.getString("nama")
                            binding.tvProfileName.text = namaUser ?: "User"
                        } else if (_binding != null) {
                            binding.tvProfileName.text = "User"
                        }
                    }
                    .addOnFailureListener { e ->
                        if (_binding != null) {
                            binding.tvProfileName.text = "User"
                        }
                    }
            }

            // Muat foto profil lokal dari SharedPreferences jika ada
            val sharedPref = requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            val savedImageUri = sharedPref.getString("profile_image_${currentUser.uid}", null)
            if (savedImageUri != null) {
                try {
                    if (_binding != null) {
                        binding.ivProfile.setImageURI(Uri.parse(savedImageUri))
                    }
                } catch (e: Exception) {
                    // Jika file terhapus atau izin hilang, biarkan default
                    if (_binding != null) {
                        binding.ivProfile.setImageResource(R.drawable.ic_logo_irrigation)
                    }
                }
            } else {
                if (_binding != null) {
                    binding.ivProfile.setImageResource(R.drawable.ic_logo_irrigation)
                }
            }
        } else {
            if (_binding != null) {
                binding.tvProfileName.text = "Tidak ada user yang login"
                binding.tvProfileEmail.text = "-"
            }
        }
    }

    // Wajib untuk mencegah memory leak pada Fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}