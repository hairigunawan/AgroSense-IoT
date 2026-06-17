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

        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvSuhu: TextView = itemView.findViewById(R.id.tvSuhu)
        private val tvKelembapan: TextView = itemView.findViewById(R.id.tvKelembapan)

        fun bind(item: RiwayatModel) {
            tvTanggal.text = item.waktu
            tvStatus.text = item.statusPompa
            tvSuhu.text = "${item.suhu}°C"
            tvKelembapan.text = "${item.kelembapan}%"
        }
    }
}