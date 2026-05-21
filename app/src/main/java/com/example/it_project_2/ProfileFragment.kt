package com.example.it_project_2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
    private val storage = FirebaseStorage.getInstance()

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

        // Klik tombol Tiga Titik di pojok kanan atas
        binding.btnMenuMore.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
            popup.menu.add("Keluar Akun")

            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Keluar Akun") {
                    // Jalankan fungsi logout yang sudah Anda punya
                    auth.signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                true
            }
            popup.show()
        }

        // Listener menu lainnya
        binding.btnEditName.setOnClickListener { showEditNameDialog() }
        binding.menuPersonal.setOnClickListener { /* aksi */ }
        binding.menuAccounts.setOnClickListener { /* aksi */ }
    }

    // --- FUNGSI BARU: MENAMPILKAN DIALOG EDIT NAMA ---
    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ubah Nama Panggilan")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(binding.tvProfileName.text.toString())
        builder.setView(input)

        builder.setPositiveButton("Simpan") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateNameInFirebase(newName)
            } else {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // --- FUNGSI BARU: UPDATE KE FIREBASE AUTH & FIRESTORE ---
    private fun updateNameInFirebase(newName: String) {
        val currentUser = auth.currentUser ?: return

        // Update di Firebase Auth (Display Name)
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update di Firestore agar permanen
                    db.collection("users").document(currentUser.uid)
                        .update("nama", newName)
                        .addOnSuccessListener {
                            binding.tvProfileName.text = newName
                            Toast.makeText(requireContext(), "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Gagal simpan ke database: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
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
                    updateUserProfilePhoto(uri.toString())
                }
            }
    }

    private fun updateUserProfilePhoto(photoUrl: String) {
        val currentUser = auth.currentUser ?: return
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(photoUrl))
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadUserProfile()
                }
            }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvProfileEmail.text = currentUser.email

            // Ambil nama dari Firestore (lebih akurat untuk CRUD)
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (_binding != null) {
                        if (document != null && document.exists()) {
                            binding.tvProfileName.text = document.getString("nama") ?: currentUser.displayName ?: "Pengguna"
                        } else {
                            binding.tvProfileName.text = currentUser.displayName ?: "Pengguna"
                        }
                    }
                }

            if (_binding != null) {
                Glide.with(this)
                    .load(currentUser.photoUrl)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(binding.ivProfile)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}