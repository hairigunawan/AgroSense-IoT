package com.example.it_project_2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.it_project_2.R
import com.example.it_project_2.model.RiwayatModel

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
        private val tvWaktu: TextView = itemView.findViewById(R.id.tvWaktu)
        private val tvSuhuRiwayat: TextView = itemView.findViewById(R.id.tvSuhuRiwayat)
        private val tvKelembapanRiwayat: TextView = itemView.findViewById(R.id.tvKelembapanRiwayat)
        private val tvPompaRiwayat: TextView = itemView.findViewById(R.id.tvPompaRiwayat)

        fun bind(riwayat: RiwayatModel) {
            tvWaktu.text = riwayat.waktu
            tvSuhuRiwayat.text = "${riwayat.suhu}°C"
            tvKelembapanRiwayat.text = "${riwayat.kelembapan_tanah}%"
            
            if (riwayat.status_pompa) {
                tvPompaRiwayat.text = "AKTIF"
                tvPompaRiwayat.setBackgroundResource(R.drawable.bg_badge_green)
            } else {
                tvPompaRiwayat.text = "MATI"
                tvPompaRiwayat.setBackgroundResource(R.drawable.bg_badge_red)
            }
        }
    }
}