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
            RiwayatModel("1", "28 Mei 2026 08:00", 28.5f, 65, true),
            RiwayatModel("2", "27 Mei 2026 18:30", 29.1f, 50, false),
            RiwayatModel("3", "27 Mei 2026 07:15", 27.8f, 70, true),
            RiwayatModel("4", "26 Mei 2026 17:45", 30.2f, 45, false),
            RiwayatModel("5", "26 Mei 2026 08:10", 28.0f, 60, true)
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