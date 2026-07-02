package com.example.it_project_2.model

data class PengaturanModel(
    val suhu_minimum: Int = 30,
    val kelembapan_udara_maksimum: Int = 60,
    val kelembapan_tanah_minimum: Int = 30,
    val kelembapan_tanah_maksimum: Int = 70,
    val durasi_pompa: Int = 60000,
    val jeda_pompa: Int = 10000
)