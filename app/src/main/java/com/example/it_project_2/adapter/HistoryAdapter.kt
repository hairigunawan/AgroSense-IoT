package com.example.it_project_2.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.it_project_2.R
import com.example.it_project_2.model.RiwayatModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val historyList = mutableListOf<RiwayatModel>()

    fun setHistoryList(list: List<RiwayatModel>) {
        historyList.clear()
        historyList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivStatusDot: ImageView = itemView.findViewById(R.id.ivStatusDot)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        private val tvJudul: TextView = itemView.findViewById(R.id.tvJudul)
        private val tvDeskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)
        private val tvTanggalSelesai: TextView = itemView.findViewById(R.id.tvTanggalSelesai)
        private val tvTanggalMulai: TextView = itemView.findViewById(R.id.tvTanggalMulai)
        private val tvDurasi: TextView = itemView.findViewById(R.id.tvDurasi)
        private val tvStatusAkhir: TextView = itemView.findViewById(R.id.tvStatusAkhir)

        fun bind(riwayat: RiwayatModel) {
            val isAuto = riwayat.mode.contains("Otomatis", ignoreCase = true)
            
            // Title & Subtitle
            tvKategori.text = if (isAuto) "PENYIRAMAN OTOMATIS" else "PENYIRAMAN MANUAL"
            tvKategori.setTextColor(if (isAuto) Color.parseColor("#4CAF50") else Color.parseColor("#2196F3"))
            ivStatusDot.setColorFilter(if (isAuto) Color.parseColor("#4CAF50") else Color.parseColor("#2196F3"))
            
            tvJudul.text = if (isAuto) "Pompa Menyiram Tanaman" else "Pompa Dinyalakan Pengguna"

            // Description based on mode
            tvDeskripsi.text = if (isAuto) {
                "Tanah mencapai batas kering sehingga sistem melakukan penyiraman hingga mencapai ${riwayat.tanah_akhir}%."
            } else {
                "Pompa berjalan selama ${riwayat.durasi} detik dan meningkatkan kelembapan tanah dari ${riwayat.tanah_awal}% menjadi ${riwayat.tanah_akhir}%."
            }

            // Footer
            val displayFormat = SimpleDateFormat("dd MMMM yyyy • HH:mm", Locale("id", "ID"))
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = try {
                riwayat.waktu_selesai?.let { parser.parse(it) }
            } catch (e: Exception) { null }

            tvTanggalMulai.text = if (date != null) "Mulai ${displayFormat.format(date)}" else riwayat.waktu_mulai
            tvTanggalSelesai.text = if (date != null) "Selesai ${displayFormat.format(date)}" else riwayat.waktu_selesai
            
            tvDurasi.text = "${riwayat.durasi} DETIK • ${riwayat.status.uppercase()}"
            tvStatusAkhir.visibility = View.GONE // Combined into tvDurasi as per design example
        }
    }
}