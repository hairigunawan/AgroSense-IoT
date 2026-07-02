package com.example.it_project_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.it_project_2.databinding.FragmentSensorBinding
import com.example.it_project_2.model.PengaturanModel
import com.example.it_project_2.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SensorFragment : Fragment() {

    private var _binding: FragmentSensorBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupSwipeRefresh()
        setupListeners()
        observeViewModel()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshSensor.setOnRefreshListener {
            // Data Firebase sudah real-time lewat observer, 
            // tapi kita paksa animasi loading sebentar untuk feedback user
            binding.swipeRefreshSensor.isRefreshing = true
            
            // Re-fetch logic if needed, or just let observers handle it
            updateFooterTimestamp()
            
            binding.root.postDelayed({
                if (_binding != null) binding.swipeRefreshSensor.isRefreshing = false
            }, 1000)
        }
        binding.swipeRefreshSensor.setColorSchemeColors(resources.getColor(R.color.green_primary, null))
    }

    private fun setupListeners() {
        // Info icons click listeners
        binding.infoSuhu.setOnClickListener {
            showInfoDialog("Batas Suhu Minimum",
                "\u2022 Range slider: 0\u00B0C \u2013 50\u00B0C\n" +
                "\u2022 Step: 1\u00B0C per geseran\n" +
                "\u2022 Nilai default: 24\u00B0C\n\n" +
                "Suhu minimum ini menjadi batas bawah aktivasi sistem pendingin. Jika suhu aktual turun di bawah nilai ini, pompa siram akan aktif.")
        }
        binding.infoKelembapanUdara.setOnClickListener {
            showInfoDialog("Batas Kelembapan Udara Maks.",
                "\u2022 Range slider: 0% \u2013 100%\n" +
                "\u2022 Step: 1% per geseran\n" +
                "\u2022 Nilai default: 85%\n\n" +
                "Kelembapan udara maksimum ini menjadi batas atas sistem. Jika kelembapan aktual lebih tinggi dari nilai ini, pompa siram tidak akan aktif.")
        }
        binding.infoKelembapanTanah.setOnClickListener {
            showInfoDialog("Batas Kelembapan Tanah",
                "\u2022 Range input: 0% \u2013 100%\n" +
                "\u2022 Nilai Min harus lebih kecil dari Maks\n" +
                "\u2022 Jika kelembapan tanah turun di bawah Min, pompa siram akan aktif otomatis\n\n" +
                "Atur rentang ideal sesuai jenis tanaman Anda.")
        }
        binding.infoDurasiPompa.setOnClickListener {
            showInfoDialog("Durasi Siram",
                "\u2022 Satuan: detik\n" +
                "\u2022 Nilai minimal: 1 detik\n" +
                "\u2022 Nilai default: 60 detik (1 menit)\n\n" +
                "Lama waktu pompa aktif dalam satu siklus penyiraman. Semakin lama durasi, semakin banyak air yang disiramkan.")
        }
        binding.infoJedaPompa.setOnClickListener {
            showInfoDialog("Jeda Antar Siram",
                "\u2022 Satuan: menit\n" +
                "\u2022 Nilai minimal: 1 menit\n" +
                "\u2022 Nilai default: 10 menit\n\n" +
                "Waktu tunggu sebelum siklus penyiraman berikutnya dimulai. Mencegah penyiraman berlebihan.")
        }

        // Update badge realtime for Suhu
        binding.sliderSuhu.addOnChangeListener { slider, value, fromUser ->
            binding.tvSuhuBadge.text = "${value.toInt()}°C"
        }

        // Update badge realtime for Kelembapan Udara
        binding.sliderKelembapanUdara.addOnChangeListener { slider, value, fromUser ->
            binding.tvKelembapanUdaraBadge.text = "${value.toInt()}%"
        }

        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            binding.tvModeLabel.text = if (isChecked) "Otomatis" else "Manual"
        }

        binding.btnSimpanPengaturan.setOnClickListener {
            simpanKeFirebase()
        }

        binding.btnBack.setOnClickListener {
            navigateToHome()
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_info_outline)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    private fun navigateToHome() {
        val activity = activity
        if (activity is MainActivity) {
            activity.navigateToHomeTab()
        }
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
        // ========== VALIDASI INPUT ==========
        val suhuMin = binding.sliderSuhu.value.toInt()
        val kelembapanUdaraMaks = binding.sliderKelembapanUdara.value.toInt()

        val kelembapanTanahMinStr = binding.etKelembapanTanahMin.text.toString()
        val kelembapanTanahMaksStr = binding.etKelembapanTanahMaks.text.toString()
        val durasiStr = binding.etDurasiPompa.text.toString()
        val jedaStr = binding.etJedaPompa.text.toString()

        // Cek input kosong
        if (kelembapanTanahMinStr.isBlank()) {
            binding.etKelembapanTanahMin.error = "Tidak boleh kosong"
            binding.etKelembapanTanahMin.requestFocus()
            return
        }
        if (kelembapanTanahMaksStr.isBlank()) {
            binding.etKelembapanTanahMaks.error = "Tidak boleh kosong"
            binding.etKelembapanTanahMaks.requestFocus()
            return
        }
        if (durasiStr.isBlank()) {
            binding.etDurasiPompa.error = "Tidak boleh kosong"
            binding.etDurasiPompa.requestFocus()
            return
        }
        if (jedaStr.isBlank()) {
            binding.etJedaPompa.error = "Tidak boleh kosong"
            binding.etJedaPompa.requestFocus()
            return
        }

        val kelembapanTanahMin = kelembapanTanahMinStr.toIntOrNull()
        val kelembapanTanahMaks = kelembapanTanahMaksStr.toIntOrNull()
        val durasiDetik = durasiStr.toIntOrNull()
        val jedaMenit = jedaStr.toIntOrNull()

        // Validasi: kelembapan tanah harus angka valid 0-100%
        if (kelembapanTanahMin == null || kelembapanTanahMin < 0 || kelembapanTanahMin > 100) {
            binding.etKelembapanTanahMin.error = "Nilai 0-100%"
            binding.etKelembapanTanahMin.requestFocus()
            return
        }
        if (kelembapanTanahMaks == null || kelembapanTanahMaks < 0 || kelembapanTanahMaks > 100) {
            binding.etKelembapanTanahMaks.error = "Nilai 0-100%"
            binding.etKelembapanTanahMaks.requestFocus()
            return
        }

        // Validasi: min tidak boleh lebih besar dari maks
        if (kelembapanTanahMin >= kelembapanTanahMaks) {
            binding.etKelembapanTanahMin.error = "Min harus < Maks"
            binding.etKelembapanTanahMaks.error = "Maks harus > Min"
            Toast.makeText(context, "Min kelembapan harus lebih kecil dari Maks", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi: durasi pompa minimal 1 detik
        if (durasiDetik == null || durasiDetik < 1) {
            binding.etDurasiPompa.error = "Minimal 1 detik"
            binding.etDurasiPompa.requestFocus()
            return
        }

        // Validasi: jeda pompa minimal 1 menit
        if (jedaMenit == null || jedaMenit < 1) {
            binding.etJedaPompa.error = "Minimal 1 menit"
            binding.etJedaPompa.requestFocus()
            return
        }

        // ========== LOADING STATE ==========
        setLoading(true)

        // Simpan mode ke Firebase saat tombol Simpan ditekan
        val mode = if (binding.switchMode.isChecked) "otomatis" else "manual"
        viewModel.setMode(mode)

        // Convert dari detik ke ms
        val durasiPompaMs = durasiDetik * 1000
        // Convert dari menit ke ms
        val jedaPompaMs = jedaMenit * 60000

        val pengaturanBaru = PengaturanModel(
            suhu_minimum = suhuMin,
            kelembapan_udara_maksimum = kelembapanUdaraMaks,
            kelembapan_tanah_minimum = kelembapanTanahMin,
            kelembapan_tanah_maksimum = kelembapanTanahMaks,
            durasi_pompa = durasiPompaMs,
            jeda_pompa = jedaPompaMs
        )

        viewModel.updatePengaturan(pengaturanBaru)
            .addOnCompleteListener { task ->
                if (_binding != null) {
                    setLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        updateFooterTimestamp()
                    }
                }
            }
            .addOnFailureListener { e ->
                if (_binding != null) {
                    Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBarSensor.visibility = View.VISIBLE
            binding.btnSimpanPengaturan.isEnabled = false
            binding.btnSimpanPengaturan.text = "Menyimpan..."
            binding.swipeRefreshSensor.isEnabled = false
        } else {
            binding.progressBarSensor.visibility = View.GONE
            binding.btnSimpanPengaturan.isEnabled = true
            binding.btnSimpanPengaturan.text = "Simpan Perubahan"
            binding.swipeRefreshSensor.isEnabled = true
        }
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
