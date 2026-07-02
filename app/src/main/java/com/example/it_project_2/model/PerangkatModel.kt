package com.example.it_project_2.model

data class PerangkatModel(
    val online: Boolean = false,
    val last_seen: Any? = null,
    val ip_perangkat: String = "0.0.0.0",
    val nama_perangkat: String = "Smart Farming 1",
    val versi_firmware: String = "v1.0.0",
    val riwayat_firmware: String = "Belum ada riwayat"
)