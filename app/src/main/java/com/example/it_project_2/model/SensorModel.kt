package com.example.it_project_2.model

data class SensorModel(
    val kelembapan_mentah: Int = 0,
    val kelembapan_persen: Int = 0,
    val suhu: Float = 0f,
    val kelembapan_udara: Int = 0,
    val status_pompa: Boolean = false,
    val sinyal_wifi: Int = 0,
    val waktu_hidup: Long = 0L,
    val terakhir_update: String = ""
)