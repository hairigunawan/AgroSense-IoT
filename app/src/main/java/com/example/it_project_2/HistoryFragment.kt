package com.example.it_project_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.it_project_2.adapter.HistoryAdapter
import com.example.it_project_2.model.RiwayatModel
import com.example.it_project_2.viewmodel.MainViewModel

class HistoryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        rvHistory = view.findViewById(R.id.rvHistory)
        progressBar = view.findViewById(R.id.progressBarRiwayat)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        rvHistory.layoutManager = LinearLayoutManager(context)
        adapter = HistoryAdapter()
        rvHistory.adapter = adapter

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            navigateToHome()
        }

        // --- INJECT DUMMY DATA LANGSUNG ---
        val dummyData = listOf(

            RiwayatModel(
                mode = "Manual",
                mulai = "08:00",
                selesai = "08:05",
                durasi = "5 Menit",
                tanggal = "28 Mei 2026"
            ),

            RiwayatModel(
                mode = "Otomatis",
                mulai = "18:30",
                selesai = "18:36",
                durasi = "6 Menit",
                tanggal = "27 Mei 2026"
            ),

            RiwayatModel(
                mode = "Manual",
                mulai = "07:15",
                selesai = "07:20",
                durasi = "5 Menit",
                tanggal = "27 Mei 2026"
            ),

            RiwayatModel(
                mode = "Otomatis",
                mulai = "17:45",
                selesai = "17:50",
                durasi = "5 Menit",
                tanggal = "26 Mei 2026"
            ),

            RiwayatModel(
                mode = "Manual",
                mulai = "08:10",
                selesai = "08:18",
                durasi = "8 Menit",
                tanggal = "26 Mei 2026"
            )

        )
        
        tvEmptyState.visibility = View.GONE
        rvHistory.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        adapter.setHistoryList(dummyData)

        // Tetap observe firebase. Jika firebase mengirim data, maka akan menimpa dummy
        viewModel.riwayatData.observe(viewLifecycleOwner) { list ->
            progressBar.visibility = View.GONE
            if (list.isNotEmpty()) {
                adapter.setHistoryList(list)
            }
        }
    }

    private fun navigateToHome() {
        val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.navigation_home
    }
}