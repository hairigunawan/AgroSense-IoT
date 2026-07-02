package com.example.it_project_2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.it_project_2.databinding.FragmentSettingBinding
import com.example.it_project_2.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

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
            val bottomSheet = NotificationBottomSheet()
            bottomSheet.show(parentFragmentManager, "NotificationBottomSheet")
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
        
        observeDeviceData()
    }
    
    private fun observeDeviceData() {
        viewModel.perangkatData.observe(viewLifecycleOwner) { perangkat ->
            val tvStatusOnline = binding.root.findViewById<TextView>(R.id.tv_status_online)
            val viewStatusDot = binding.root.findViewById<View>(R.id.view_status_dot)
            val tvLastSeen = binding.root.findViewById<TextView>(R.id.tv_last_seen)
            val tvFirmwareVersion = binding.root.findViewById<TextView>(R.id.tv_firmware_version)
            val tvFirmwareHistory = binding.root.findViewById<TextView>(R.id.tv_firmware_history)
            val tvDeviceName = binding.root.findViewById<TextView>(R.id.tv_device_name)
            val tvDeviceIp = binding.root.findViewById<TextView>(R.id.tv_device_ip)
            
            tvDeviceName?.text = perangkat.nama_perangkat
            tvDeviceIp?.text = perangkat.ip_perangkat
            
            if (perangkat.online) {
                tvStatusOnline?.text = "Online"
                tvStatusOnline?.setTextColor(resources.getColor(R.color.white, null))
                viewStatusDot?.setBackgroundResource(R.drawable.dot_green)
                val statusContainer = tvStatusOnline?.parent as? LinearLayout
                statusContainer?.setBackgroundResource(R.drawable.bg_badge_green)
            } else {
                tvStatusOnline?.text = "Offline"
                tvStatusOnline?.setTextColor(resources.getColor(R.color.text_dark, null))
                viewStatusDot?.setBackgroundResource(android.R.color.darker_gray)
                val statusContainer = tvStatusOnline?.parent as? LinearLayout
                statusContainer?.setBackgroundResource(R.drawable.bg_chip_light_blue) // or a grey background
            }
            
            val lastSeenFormatted = formatLastSeen(perangkat.last_seen)
            tvLastSeen?.text = "Terakhir online: $lastSeenFormatted"
            
            tvFirmwareVersion?.text = perangkat.versi_firmware
            tvFirmwareHistory?.text = "Riwayat Update: ${perangkat.riwayat_firmware}"
        }
    }
    
    private fun formatLastSeen(lastSeenObj: Any?): String {
        if (lastSeenObj == null) return "Belum diketahui"
        return try {
            when (lastSeenObj) {
                is String -> lastSeenObj
                is Long -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
                    sdf.format(Date(lastSeenObj))
                }
                is Double -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
                    sdf.format(Date(lastSeenObj.toLong()))
                }
                else -> lastSeenObj.toString()
            }
        } catch (e: Exception) {
            "Format tidak valid"
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
                            if (_binding != null) {
                                binding.tvProfileName.text = newName
                                Toast.makeText(requireContext(), "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            if (_binding != null) {
                                binding.tvProfileName.text = newName
                                Toast.makeText(requireContext(), "Nama diperbarui (hanya lokal)", Toast.LENGTH_SHORT).show()
                            }
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
                // If it's a google photo, we can request a higher res version by modifying the URL
                // Google URLs often end with =s96-c
                var highResUrl = photoUrl.toString()
                if (highResUrl.contains("googleusercontent.com")) {
                    highResUrl = highResUrl.replace("s96-c", "s400-c")
                }
                
                Glide.with(this)
                    .load(highResUrl)
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
        val activity = activity
        if (activity is MainActivity) {
            activity.navigateToHomeTab()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
