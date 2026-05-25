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
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.it_project_2.databinding.FragmentProfileBinding
import com.example.it_project_2.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() 

    private lateinit var viewModel: MainViewModel

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                uploadImageToFirebase(it)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return
        
        Toast.makeText(requireContext(), "Mengunggah foto profil...", Toast.LENGTH_SHORT).show()
        
        val storageRef = storage.reference.child("profile_images/${currentUser.uid}.jpg")
        
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfile(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal mengunggah foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateUserProfile(photoUrl: String) {
        val currentUser = auth.currentUser ?: return
        
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(photoUrl))
            .build()
            
        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    loadUserProfile() // Reload image with Glide
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        loadUserProfile()

        binding.cardProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnBackProfile.setOnClickListener {
            navigateToHome()
        }

        // Observe Device Status
        viewModel.perangkatData.observe(viewLifecycleOwner) { perangkat ->
            binding.tvDeviceName.text = perangkat.nama_perangkat
            binding.tvDeviceIp.text = perangkat.ip_perangkat
            binding.tvDeviceFirmware.text = perangkat.versi_firmware
        }

        viewModel.sensorData.observe(viewLifecycleOwner) { sensor ->
            // uptime in milliseconds to readable format (e.g. 1d 2h 30m)
            val uptime = sensor.waktu_hidup
            val days = uptime / (24 * 3600000)
            val hours = (uptime % (24 * 3600000)) / 3600000
            val minutes = (uptime % 3600000) / 60000
            
            val uptimeStr = if (days > 0) {
                "${days}h ${hours}j ${minutes}m"
            } else if (hours > 0) {
                "${hours}j ${minutes}m"
            } else {
                "${minutes}m"
            }
            binding.tvDeviceUptime.text = uptimeStr
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            binding.tvProfileEmail.text = currentUser.email

            val authName = currentUser.displayName
            if (!authName.isNullOrEmpty()) {
                if (_binding != null) {
                    binding.tvProfileName.text = authName
                }
            } else {
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (_binding != null && document != null && document.exists()) {
                            val namaUser = document.getString("nama")
                            binding.tvProfileName.text = namaUser ?: "Pengguna"
                        } else if (_binding != null) {
                            binding.tvProfileName.text = "Pengguna"
                        }
                    }
                    .addOnFailureListener {
                        if (_binding != null) {
                            binding.tvProfileName.text = "Pengguna"
                        }
                    }
            }

            if (_binding != null) {
                if (currentUser.photoUrl != null) {
                    Glide.with(this)
                        .load(currentUser.photoUrl)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.user)
                }
            }
        } else {
            if (_binding != null) {
                binding.tvProfileName.text = "Tidak ada user yang login"
                binding.tvProfileEmail.text = "-"
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