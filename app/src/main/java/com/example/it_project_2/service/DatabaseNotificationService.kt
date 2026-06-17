package com.example.it_project_2.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.it_project_2.MainActivity
import com.example.it_project_2.R
import com.example.it_project_2.model.NotificationModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class DatabaseNotificationService : Service() {

    private val CHANNEL_ID = "agrosense_background"
    private var isInitialData = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        val notification = createForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
        
        listenForNewNotifications()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY membuat service akan otomatis direstart oleh Android jika tiba-tiba dihentikan
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemantauan Latar Belakang",
                NotificationManager.IMPORTANCE_LOW // Low = tidak ada suara/getar untuk notif status ini
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AgroSense Aktif")
            .setContentText("Aplikasi memantau kondisi kebun dari latar belakang.")
            .setSmallIcon(R.drawable.notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun listenForNewNotifications() {
        val dbRef = FirebaseDatabase.getInstance().getReference("notifications")
        // limitToLast(1) mengambil 1 data paling terakhir
        val query = dbRef.orderByChild("timestamp").limitToLast(1)

        query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (isInitialData) {
                    // Ketika service baru berjalan, ia akan mengambil 1 data terakhir dari database.
                    // Kita abaikan ini agar tidak memunculkan notifikasi "basi".
                    isInitialData = false
                    return
                }
                
                // Jika isInitialData sudah false, berarti ini adalah benar-benar data BARU yang baru saja masuk
                val notif = snapshot.getValue(NotificationModel::class.java)
                if (notif != null) {
                    showPushNotification(notif)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseNotifService", "Error listening: ${error.message}")
            }
        })
    }

    private fun showPushNotification(model: NotificationModel) {
        val alertChannelId = "agrosense_alerts"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel khusus untuk peringatan (dengan suara dan getaran)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                alertChannelId,
                "Peringatan AgroSense",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, alertChannelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(model.title)
            .setContentText(model.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Menyalakan suara & getar default HP
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        manager.notify(notificationId, notification)
    }
}
