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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMulai: TextView = itemView.findViewById(R.id.tvwaktuMulai)
        private val tvSelesai: TextView = itemView.findViewById(R.id.tvwaktuSelesai)
        private val tvDurasi: TextView = itemView.findViewById(R.id.tvDurasi)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val statusDot: View = itemView.findViewById(R.id.viewStatusDot)

        fun bind(item: RiwayatModel) {

            // Isi data
            tvMulai.text = "Mulai : ${item.mulai}"
            tvSelesai.text = "Selesai : ${item.selesai}"
            tvDurasi.text = "Durasi : ${item.durasi}"
            tvTanggal.text = item.tanggal

            // Mode penyiraman
            if (item.mode.equals("Manual", ignoreCase = true)) {

                tvTitle.text = "Penyiraman Manual"
                statusDot.setBackgroundResource(R.drawable.circle_red)

            } else {

                tvTitle.text = "Penyiraman Otomatis"
                statusDot.setBackgroundResource(R.drawable.dot_green)

            }
        }
    }
}