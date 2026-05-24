package com.example.it_project_2.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.it_project_2.model.*
import com.google.firebase.database.*

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
                snapshot.getValue(SensorModel::class.java)?.let {
                    data.postValue(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
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

    fun updatePengaturan(pengaturan: PengaturanModel) {
        pengaturanRef.setValue(pengaturan)
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

    // Riwayat Data
    fun getRiwayatData(): LiveData<List<RiwayatModel>> {
        val dataList = MutableLiveData<List<RiwayatModel>>()
        riwayatRef.orderByKey().limitToLast(50).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<RiwayatModel>()
                for (child in snapshot.children) {
                    val riwayat = child.getValue(RiwayatModel::class.java)
                    riwayat?.let {
                        it.id = child.key ?: ""
                        list.add(it)
                    }
                }
                list.reverse() // Newest first
                dataList.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        return dataList
    }
}