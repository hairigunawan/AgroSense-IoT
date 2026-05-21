package com.example.it_project_2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.it_project_2.model.*
import com.example.it_project_2.repository.FirebaseRepository

class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    val sensorData: LiveData<SensorModel> = repository.getSensorData()
    val kontrolData: LiveData<KontrolModel> = repository.getKontrolData()
    val pengaturanData: LiveData<PengaturanModel> = repository.getPengaturanData()
    val perangkatData: LiveData<PerangkatModel> = repository.getPerangkatData()
    val riwayatData: LiveData<List<RiwayatModel>> = repository.getRiwayatData()

    fun setPompa(isOn: Boolean) {
        repository.updateKontrolPompa(isOn)
    }

    fun setMode(mode: String) {
        repository.updateKontrolMode(mode)
    }

    fun updatePengaturan(pengaturan: PengaturanModel) {
        repository.updatePengaturan(pengaturan)
    }
}