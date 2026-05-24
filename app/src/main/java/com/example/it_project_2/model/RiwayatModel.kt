package com.example.it_project_2.model

data class RiwayatModel(
    var id: String = "",
    val waktu: String = "",
    val suhu: Float = 0f,
    val kelembapan_tanah: Int = 0,
    val status_pompa: Boolean = false
)