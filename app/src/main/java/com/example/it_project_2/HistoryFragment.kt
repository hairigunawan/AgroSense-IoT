package com.example.it_project_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        
        val rvHistory = view.findViewById<RecyclerView>(R.id.rvHistory)
        val layoutEmptyState = view.findViewById<LinearLayout>(R.id.layoutEmptyState)

        // Data dummy untuk RecyclerView
        val dummyData = listOf(
            HistoryItem("24 Januari 2025", "16:00", "SELESAI"),
            HistoryItem("23 Januari 2025", "16:05", "SELESAI"),
            HistoryItem("22 Januari 2025", "16:10", "SELESAI"),
            HistoryItem("21 Januari 2025", "16:00", "SELESAI"),
            HistoryItem("20 Januari 2025", "15:55", "SELESAI"),
            HistoryItem("19 Januari 2025", "16:02", "SELESAI")
        )

        if (dummyData.isEmpty()) {
            rvHistory.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvHistory.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            
            rvHistory.layoutManager = LinearLayoutManager(requireContext())
            rvHistory.adapter = HistoryAdapter(dummyData)
            
            // Re-run layout animation when opening
            rvHistory.scheduleLayoutAnimation()
        }

        return view
    }
}