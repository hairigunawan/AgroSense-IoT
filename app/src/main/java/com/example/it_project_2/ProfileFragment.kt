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
import com.bumptech.glide.Glide
import com.example.it_project_2.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() // Menggunakan inisialisasi default dari google-services.json

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
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.ic_person)
                }
            }
        } else {
            if (_binding != null) {
                binding.tvProfileName.text = "Tidak ada user yang login"
                binding.tvProfileEmail.text = "-"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}