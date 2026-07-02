package com.example.it_project_2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.it_project_2.adapter.HistoryAdapter
import com.example.it_project_2.model.RiwayatModel
import com.example.it_project_2.viewmodel.MainViewModel
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var chipGroupMode: ChipGroup
    private lateinit var etSearchHistory: EditText
    private lateinit var swipeRefreshRiwayat: SwipeRefreshLayout

    private var allHistoryList: List<RiwayatModel> = emptyList()

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
        chipGroupMode = view.findViewById(R.id.chipGroupMode)
        etSearchHistory = view.findViewById(R.id.etSearchHistory)
        swipeRefreshRiwayat = view.findViewById(R.id.swipeRefreshRiwayat)

        rvHistory.layoutManager = LinearLayoutManager(context)
        adapter = HistoryAdapter()
        rvHistory.adapter = adapter

        swipeRefreshRiwayat.setOnRefreshListener {
            swipeRefreshRiwayat.isRefreshing = false
        }
        swipeRefreshRiwayat.setColorSchemeColors(resources.getColor(R.color.green_primary, null))

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            navigateToHome()
        }

        chipGroupMode.setOnCheckedChangeListener { _, checkedId ->
            filterData()
        }

        etSearchHistory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tampilkan progress bar saat menunggu data dari Firebase
        progressBar.visibility = View.VISIBLE
        rvHistory.visibility = View.GONE
        tvEmptyState.visibility = View.GONE

        // Observe data asli dari Firebase
        viewModel.riwayatData.observe(viewLifecycleOwner) { list ->
            progressBar.visibility = View.GONE
            allHistoryList = list
            filterData()
        }
    }

    private fun filterData() {
        val searchQuery = etSearchHistory.text.toString().lowercase()
        val checkedId = chipGroupMode.checkedChipId

        // Mapping bulan ke angka format -MM-
        val bulanMap = mapOf(
            "januari" to "-01-", "februari" to "-02-", "maret" to "-03-",
            "april" to "-04-", "mei" to "-05-", "juni" to "-06-",
            "juli" to "-07-", "agustus" to "-08-", "september" to "-09-",
            "oktober" to "-10-", "november" to "-11-", "desember" to "-12-"
        )

        // Ubah kata kunci bulan menjadi angka jika ditemukan di mapping
        val searchForDate = bulanMap[searchQuery] ?: searchQuery

        val filteredList = allHistoryList.filter { riwayat ->
            val matchesMode = when (checkedId) {
                R.id.chipOtomatis -> riwayat.mode.contains("Otomatis", true)
                R.id.chipManual -> riwayat.mode.contains("Manual", true)
                else -> true
            }

            // Pencarian teks HANYA berdasarkan tanggal (dd, MM, yyyy) dari waktu_mulai
            val matchesDateSearch = riwayat.waktu_mulai.lowercase().contains(searchForDate)

            matchesMode && matchesDateSearch
        }

        if (filteredList.isNotEmpty()) {
            rvHistory.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            adapter.setHistoryList(filteredList)
        } else {
            rvHistory.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            adapter.setHistoryList(emptyList())
        }
    }

    private fun navigateToHome() {
        val activity = activity
        if (activity is MainActivity) {
            activity.navigateToHomeTab()
        }
    }
}