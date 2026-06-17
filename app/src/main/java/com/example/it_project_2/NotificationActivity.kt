package com.example.it_project_2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.it_project_2.adapter.NotificationAdapter
import com.example.it_project_2.databinding.ActivityNotificationBinding
import com.example.it_project_2.model.NotificationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private val notificationList = ArrayList<NotificationModel>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        fetchNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notificationList)
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun fetchNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        val databaseRef = FirebaseDatabase.getInstance().getReference("notifications")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                if (snapshot.exists()) {
                    for (notifSnapshot in snapshot.children) {
                        val notification = notifSnapshot.getValue(NotificationModel::class.java)
                        if (notification != null) {
                            notificationList.add(notification)
                        }
                    }
                    // Sort descending by timestamp (newest first)
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
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@NotificationActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
