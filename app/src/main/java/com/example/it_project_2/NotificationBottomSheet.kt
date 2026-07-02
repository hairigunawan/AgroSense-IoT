package com.example.it_project_2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.it_project_2.adapter.NotificationAdapter
import com.example.it_project_2.databinding.LayoutNotificationBottomSheetBinding
import com.example.it_project_2.model.NotificationModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutNotificationBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val notificationList = ArrayList<NotificationModel>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private val PREF_NAME = "notif_prefs"
    private val KEY_READ_IDS = "read_ids"
    private var readIdsSet = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutNotificationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        readIdsSet = sharedPrefs.getStringSet(KEY_READ_IDS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        setupRecyclerView()
        fetchNotifications()

        binding.btnMarkRead.setOnClickListener {
            markAllAsRead()
        }

        binding.btnClearAll.setOnClickListener {
            clearAllNotifications()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList, readIdsSet)
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    private fun fetchNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        val databaseRef = FirebaseDatabase.getInstance().getReference("notifications")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                notificationList.clear()
                if (snapshot.exists()) {
                    for (notifSnapshot in snapshot.children) {
                        val notification = notifSnapshot.getValue(NotificationModel::class.java)
                        if (notification != null) {
                            val notifWithId = if (notification.id.isEmpty()) {
                                notification.copy(id = notifSnapshot.key ?: "")
                            } else notification
                            notificationList.add(notifWithId)
                        }
                    }
                    notificationList.sortByDescending { it.timestamp }
                    
                    binding.rvNotifications.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                } else {
                    binding.rvNotifications.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markAllAsRead() {
        val allIds = notificationList.map { it.id }.toSet()
        readIdsSet.addAll(allIds)
        sharedPrefs.edit().putStringSet(KEY_READ_IDS, readIdsSet).apply()
        adapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Semua notifikasi ditandai sudah dibaca", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllNotifications() {
        if (notificationList.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada notifikasi untuk dihapus", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.rvNotifications.visibility = View.GONE
        
        val databaseRef = FirebaseDatabase.getInstance().getReference("notifications")
        databaseRef.removeValue().addOnCompleteListener { task ->
            if (_binding == null) return@addOnCompleteListener
            if (task.isSuccessful) {
                notificationList.clear()
                readIdsSet.clear()
                sharedPrefs.edit().remove(KEY_READ_IDS).apply()
                adapter.notifyDataSetChanged()
                
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                
                Toast.makeText(requireContext(), "Semua notifikasi berhasil dihapus", Toast.LENGTH_SHORT).show()
            } else {
                binding.progressBar.visibility = View.GONE
                binding.rvNotifications.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Gagal menghapus notifikasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
