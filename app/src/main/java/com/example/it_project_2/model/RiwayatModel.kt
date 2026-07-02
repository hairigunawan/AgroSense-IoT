package com.example.it_project_2.model

data class RiwayatModel(
    var id: String = "",
    var alasan: String = "",
    var durasi: Int = 0,
    var kelembapan_udara: Double = 0.0,
    var mode: String = "",
    var status: String = "Berhasil",
    var suhu: Double = 0.0,
    var tanah_awal: Int = 0,
    var tanah_akhir: Int = 0,
    var waktu_mulai: String = "",
    var waktu_selesai: String = "",
    var timestamp: Long = 0L
)
