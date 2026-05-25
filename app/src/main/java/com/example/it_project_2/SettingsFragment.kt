package com.example.it_project_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.it_project_2.databinding.FragmentSettingsBinding
import com.example.it_project_2.model.PengaturanModel
import com.example.it_project_2.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Update badge realtime for Suhu
        binding.sliderSuhu.addOnChangeListener { slider, value, fromUser ->
            binding.tvSuhuBadge.text = "${value.toInt()}°C"
        }

        // Update badge realtime for Kelembapan Udara
        binding.sliderKelembapanUdara.addOnChangeListener { slider, value, fromUser ->
            binding.tvKelembapanUdaraBadge.text = "${value.toInt()}%"
        }

        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) "otomatis" else "manual"
            viewModel.setMode(mode)
            binding.tvModeLabel.text = if (isChecked) "Otomatis" else "Manual"
        }

        binding.btnSimpanPengaturan.setOnClickListener {
            simpanKeFirebase()
        }

        binding.btnBackSettings.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = R.id.navigation_home
    }

    private fun observeViewModel() {
        viewModel.pengaturanData.observe(viewLifecycleOwner) { pengaturan ->
            // Update Slider values
            val suhu = pengaturan.suhu_minimum?.toFloat() ?: 24f
            val kelembapanUdara = pengaturan.kelembapan_udara_maksimum?.toFloat() ?: 85f
            
            // Validate limits for sliders before setting to avoid crash
            if (suhu >= binding.sliderSuhu.valueFrom && suhu <= binding.sliderSuhu.valueTo) {
                binding.sliderSuhu.value = suhu
                binding.tvSuhuBadge.text = "${suhu.toInt()}°C"
            }
            if (kelembapanUdara >= binding.sliderKelembapanUdara.valueFrom && kelembapanUdara <= binding.sliderKelembapanUdara.valueTo) {
                binding.sliderKelembapanUdara.value = kelembapanUdara
                binding.tvKelembapanUdaraBadge.text = "${kelembapanUdara.toInt()}%"
            }

            // Update EditText values
            binding.etKelembapanTanahMin.setText(pengaturan.kelembapan_tanah_minimum.toString())
            binding.etKelembapanTanahMaks.setText(pengaturan.kelembapan_tanah_maksimum.toString())

            val durasiDetik = (pengaturan.durasi_pompa ?: 60000) / 1000
            val jedaMenit = (pengaturan.jeda_pompa ?: 600000) / 60000
            
            binding.etDurasiPompa.setText(durasiDetik.toString())
            binding.etJedaPompa.setText(jedaMenit.toString())
            
            // Set timestamp terakhir diperbarui mock up / current
            updateFooterTimestamp()
        }

        viewModel.kontrolData.observe(viewLifecycleOwner) { kontrol ->
            val isOtomatis = kontrol.mode == "otomatis"
            if (binding.switchMode.isChecked != isOtomatis) {
                binding.switchMode.isChecked = isOtomatis
            }
            binding.tvModeLabel.text = if (isOtomatis) "Otomatis" else "Manual"
        }
    }

    private fun simpanKeFirebase() {
        val suhuMin = binding.sliderSuhu.value.toInt()
        val kelembapanUdaraMaks = binding.sliderKelembapanUdara.value.toInt()
        val kelembapanTanahMin = binding.etKelembapanTanahMin.text.toString().toIntOrNull() ?: 30
        val kelembapanTanahMaks = binding.etKelembapanTanahMaks.text.toString().toIntOrNull() ?: 70
        
        // Convert dari detik ke ms
        val durasiPompaMs = (binding.etDurasiPompa.text.toString().toIntOrNull() ?: 60) * 1000
        // Convert dari menit ke ms
        val jedaPompaMs = (binding.etJedaPompa.text.toString().toIntOrNull() ?: 10) * 60000

        val pengaturanBaru = PengaturanModel(
            suhu_minimum = suhuMin,
            kelembapan_udara_maksimum = kelembapanUdaraMaks,
            kelembapan_tanah_minimum = kelembapanTanahMin,
            kelembapan_tanah_maksimum = kelembapanTanahMaks,
            durasi_pompa = durasiPompaMs,
            jeda_pompa = jedaPompaMs
        )

        viewModel.updatePengaturan(pengaturanBaru)
        
        Toast.makeText(context, "Pengaturan Berhasil Disimpan ke Firebase", Toast.LENGTH_SHORT).show()
        updateFooterTimestamp()
    }
    
    private fun updateFooterTimestamp() {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
        val currentDate = sdf.format(Date())
        binding.tvTerakhirDiperbarui.text = "Terakhir diperbarui: $currentDate"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
