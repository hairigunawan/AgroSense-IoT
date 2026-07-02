package com.example.it_project_2.model

import com.google.firebase.database.PropertyName

data class SensorModel(
    @get:PropertyName("kelembapan_mentah")
    @set:PropertyName("kelembapan_mentah")
    var kelembapan_mentah: Int = 0,

    @get:PropertyName("kelembapan") // Sesuai export Firebase Anda
    @set:PropertyName("kelembapan")
    var kelembapan_persen: Int = 0,

    @get:PropertyName("suhu")
    @set:PropertyName("suhu")
    var suhu: Float = 0f,

    @get:PropertyName("kelembapan_udara")
    @set:PropertyName("kelembapan_udara")
    var kelembapan_udara: Int = 0,

    @get:PropertyName("statusPompa") // Mengikuti format camelCase di riwayat
    @set:PropertyName("statusPompa")
    var status_pompa: String = "OFF",

    @get:PropertyName("sinyal_wifi")
    @set:PropertyName("sinyal_wifi")
    var sinyal_wifi: Int = 0,

    @get:PropertyName("waktu_hidup")
    @set:PropertyName("waktu_hidup")
    var waktu_hidup: Long = 0L,

    @get:PropertyName("terakhir_update")
    @set:PropertyName("terakhir_update")
    var terakhir_update: String = ""
)