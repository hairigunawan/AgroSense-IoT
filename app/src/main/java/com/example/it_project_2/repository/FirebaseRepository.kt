package com.example.it_project_2.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.it_project_2.model.*
import com.google.firebase.database.*
import android.util.Log

class FirebaseRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // References
    private val sensorRef = database.getReference("sensor")
    private val kontrolRef = database.getReference("kontrol")
    private val pengaturanRef = database.getReference("pengaturan")
    private val perangkatRef = database.getReference("perangkat")
    private val riwayatRef = database.getReference("riwayat")

    // Sensor Data
    fun getSensorData(): LiveData<SensorModel> {
        val data = MutableLiveData<SensorModel>()
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "Node /sensor tidak ditemukan")
                    return
                }

                // Nilai numerik dari RTDB dapat diterima sebagai Long, Double,
                // atau String, tergantung cara perangkat IoT menulis data.
                // Baca secara eksplisit agar perubahan kelembapan tidak gagal
                // hanya karena tipe atau nama field berbeda.
                val sensor = SensorModel(
                    kelembapan_mentah = snapshot.intValue("kelembapan_mentah"),
                    kelembapan_persen = snapshot.intValue(
                        "kelembapan",
                        "kelembapan_persen",
                        "kelembapan_tanah"
                    ),
                    suhu = snapshot.floatValue("suhu"),
                    kelembapan_udara = snapshot.intValue("kelembapan_udara"),
                    status_pompa = snapshot.stringValue("statusPompa", "status_pompa")
                        ?: "OFF",
                    sinyal_wifi = snapshot.intValue("sinyal_wifi"),
                    waktu_hidup = snapshot.longValue("waktu_hidup"),
                    terakhir_update = snapshot.stringValue("terakhir_update").orEmpty()
                )

                Log.d(
                    TAG,
                    "Sensor diperbarui: kelembapan=${sensor.kelembapan_persen}, " +
                        "suhu=${sensor.suhu}"
                )
                data.value = sensor
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Gagal membaca /sensor: ${error.message}", error.toException())
            }
        })
        return data
    }

    // Kontrol Data
    fun getKontrolData(): LiveData<KontrolModel> {
        val data = MutableLiveData<KontrolModel>()
        kontrolRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(KontrolModel::class.java)?.let {
                    data.postValue(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return data
    }

    // Update Kontrol
    fun updateKontrolPompa(isOn: Boolean) {
        kontrolRef.child("pompa").setValue(isOn)
    }

    fun updateKontrolMode(mode: String) {
        kontrolRef.child("mode").setValue(mode)
    }

    // Pengaturan Data
    fun getPengaturanData(): LiveData<PengaturanModel> {
        val data = MutableLiveData<PengaturanModel>()
        pengaturanRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(PengaturanModel::class.java)?.let {
                    data.postValue(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return data
    }

    fun updatePengaturan(pengaturan: PengaturanModel): com.google.android.gms.tasks.Task<Void> {
        return pengaturanRef.setValue(pengaturan)
    }

    fun pushRiwayat(riwayat: RiwayatModel) {
        riwayatRef.push().setValue(riwayat)
    }

    // Perangkat Data
    fun getPerangkatData(): LiveData<PerangkatModel> {
        val data = MutableLiveData<PerangkatModel>()
        perangkatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(PerangkatModel::class.java)?.let {
                    data.postValue(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return data
    }
    fun getRiwayatData(): LiveData<List<RiwayatModel>> {
        val dataList = MutableLiveData<List<RiwayatModel>>()
        // Assuming 'timestamp' is used for sorting in Firebase. 
        // If the structure changed to support 'orderByChild("timestamp")', ensure the index exists in Firebase.
        riwayatRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<RiwayatModel>()

                Log.d("RiwayatDebug", "Jumlah data dari Firebase: ${snapshot.childrenCount}")

                for (child in snapshot.children) {
                    try {
                        val riwayat = child.getValue(RiwayatModel::class.java)
                        riwayat?.let {
                            it.id = child.key ?: ""
                            list.add(it)
                        }
                    } catch (e: Exception) {
                        Log.e("RiwayatDebug", "Error parsing data: ${e.message}")
                    }
                }
                // Sort descending by timestamp in memory to be safe
                list.sortByDescending { it.timestamp }
                dataList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("RiwayatDebug", "Database error: ${error.message}")
            }
        })
        return dataList
    }

    private fun DataSnapshot.firstValue(vararg names: String): Any? =
        names.firstNotNullOfOrNull { name -> child(name).value }

    private fun DataSnapshot.intValue(vararg names: String): Int =
        when (val raw = firstValue(*names)) {
            is Number -> raw.toInt()
            is String -> raw.toDoubleOrNull()?.toInt() ?: 0
            else -> 0
        }

    private fun DataSnapshot.longValue(vararg names: String): Long =
        when (val raw = firstValue(*names)) {
            is Number -> raw.toLong()
            is String -> raw.toDoubleOrNull()?.toLong() ?: 0L
            else -> 0L
        }

    private fun DataSnapshot.floatValue(vararg names: String): Float =
        when (val raw = firstValue(*names)) {
            is Number -> raw.toFloat()
            is String -> raw.toFloatOrNull() ?: 0f
            else -> 0f
        }

    private fun DataSnapshot.stringValue(vararg names: String): String? =
        firstValue(*names)?.toString()

    private companion object {
        const val TAG = "FirebaseRepository"
    }
}
