package com.example.it_project_2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.it_project_2.model.*
import com.example.it_project_2.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SensorHistoryPoint(val value: Float, val timestamp: Long)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    val sensorData: LiveData<SensorModel> = repository.getSensorData()
    val kontrolData: LiveData<KontrolModel> = repository.getKontrolData()
    val pengaturanData: LiveData<PengaturanModel> = repository.getPengaturanData()
    val perangkatData: LiveData<PerangkatModel> = repository.getPerangkatData()
    val riwayatData: LiveData<List<RiwayatModel>> = repository.getRiwayatData()

    // Temp state for pump cycle
    private var pumpStartedData: RiwayatModel? = null

    // Rolling windows for charts (max 20 points)
    private val _suhuHistory = MutableLiveData<List<SensorHistoryPoint>>(emptyList())
    val suhuHistory: LiveData<List<SensorHistoryPoint>> = _suhuHistory

    private val _kelembapanTanahHistory = MutableLiveData<List<SensorHistoryPoint>>(emptyList())
    val kelembapanTanahHistory: LiveData<List<SensorHistoryPoint>> = _kelembapanTanahHistory

    init {
        // Observe sensor data to build a local rolling history
        sensorData.observeForever { sensor ->
            val now = System.currentTimeMillis()

            appendHistoryPointIfChanged(_suhuHistory, sensor.suhu.toFloat(), now)
            appendHistoryPointIfChanged(_kelembapanTanahHistory, sensor.kelembapan_persen.toFloat(), now)
            
            // Logic to track pump status and build history entry
            handlePumpHistoryTracking(sensor)
        }
    }
    
    // State to track pump cycle
    private var pumpStartTime: Long = 0L
    private var pumpStartSensor: SensorModel? = null

    private fun handlePumpHistoryTracking(sensor: SensorModel) {
        val isPompaOn = sensor.status_pompa.equals("ON", ignoreCase = true)
        
        if (isPompaOn) {
            // Hanya simpan data awal saat pertama kali ON
            if (pumpStartTime <= 0L) {
                pumpStartTime = System.currentTimeMillis()
                pumpStartSensor = sensor
            }
        } else if (pumpStartTime > 0L) {
            // Pompa mati, buat satu record gabungan
            val durationMs = System.currentTimeMillis() - pumpStartTime
            val durationSeconds = (durationMs / 1000).toInt()

            // Ambil mode dari kontrolData
            val mode = kontrolData.value?.mode ?: "Otomatis"

            // Buat record baru sesuai model
            val record = RiwayatModel(
                waktu_mulai = pumpStartSensor?.terakhir_update ?: "",
                waktu_selesai = sensor.terakhir_update,
                durasi = durationSeconds,
                mode = mode,
                alasan = if (mode == "Otomatis") "Tanah mencapai batas kering" else "Perintah manual pengguna",
                suhu = sensor.suhu.toDouble(),
                kelembapan_udara = sensor.kelembapan_udara.toDouble(),
                tanah_awal = pumpStartSensor?.kelembapan_persen ?: 0,
                tanah_akhir = sensor.kelembapan_persen,
                status = "Berhasil"
            )

            repository.pushRiwayat(record)

            // Reset state
            pumpStartTime = 0L
            pumpStartSensor = null
        }
    }

    private fun appendHistoryPointIfChanged(
        historyLiveData: MutableLiveData<List<SensorHistoryPoint>>,
        newValue: Float,
        timestamp: Long
    ) {
        val currentList = historyLiveData.value?.toMutableList() ?: mutableListOf()
        val lastValue = currentList.lastOrNull()?.value

        if (lastValue == newValue) {
            return
        }

        currentList.add(SensorHistoryPoint(newValue, timestamp))
        if (currentList.size > 20) currentList.removeAt(0)
        historyLiveData.postValue(currentList)
    }

    fun setPompa(isOn: Boolean) {
        repository.updateKontrolPompa(isOn)
    }

    fun setMode(mode: String) {
        repository.updateKontrolMode(mode)
    }

    fun updatePengaturan(pengaturan: PengaturanModel): com.google.android.gms.tasks.Task<Void> {
        return repository.updatePengaturan(pengaturan)
    }
}
