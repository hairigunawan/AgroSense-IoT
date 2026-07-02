package com.example.it_project_2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.it_project_2.viewmodel.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // HAPUS tanda '}' yang nyasar dan perbaiki deklarasi jika pakai view binding.
    // Karena Anda dominan menggunakan findViewById di bawah, variabel binding ini bisa dihapus atau diabaikan dulu.
    private lateinit var viewModel: MainViewModel
    private lateinit var swipeRefreshHome: SwipeRefreshLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val WEATHER_API_KEY = "440b67ccfc304047819164625260205"

    private lateinit var tvSuhu: TextView
    private lateinit var tvKelembapanTanah: TextView
    private lateinit var tvKelembapanUdara: TextView

    private lateinit var tvLocation: TextView
    private lateinit var tvWeatherTemp: TextView

    private lateinit var switchPompa: SwitchMaterial
    private lateinit var tvModeStatus: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvGreeting: TextView

    private lateinit var tvUser: TextView
    private lateinit var tvSuhuStatus: TextView
    private lateinit var tvMoistureStatus: TextView

    private lateinit var tvWeatherHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var progressHumidity: android.widget.ProgressBar

    private var pumpStatusState by mutableStateOf(false)

    private lateinit var badgeNotification: View

    private lateinit var ivWeatherIcon3D: ImageView
    private lateinit var ivWeatherBg: ImageView
    private lateinit var layoutWeatherBg: View
    private lateinit var tvHomeWeatherCondition: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndWeather()
        } else {
            getWeatherSummary("-3.7921389,114.8079722")
            Toast.makeText(requireContext(), "Izin lokasi ditolak, menampilkan cuaca default.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        tvSuhu = view.findViewById(R.id.suhuValue)
        tvKelembapanTanah = view.findViewById(R.id.kelembapanTanahValue)
        tvKelembapanUdara = view.findViewById(R.id.kelembapanUdaraValue)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvWeatherTemp = view.findViewById(R.id.tvWeatherTemp)
        switchPompa = view.findViewById(R.id.switchPompa)
        
        // Setup Compose View for Pump Status
        val composePumpStatus = view.findViewById<androidx.compose.ui.platform.ComposeView>(R.id.composePumpStatus)
        composePumpStatus.setContent {
            val isOn = pumpStatusState
            com.example.it_project_2.ui.components.PumpStatusIndicator(isOn = isOn)
        }
        
        tvModeStatus = view.findViewById(R.id.tvModeStatus)
        tvDate = view.findViewById(R.id.tvDate)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvUser = view.findViewById(R.id.tvUser)
        tvWeatherHumidity = view.findViewById(R.id.tvWeatherHumidity)
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed)

        tvSuhuStatus = view.findViewById(R.id.tvSuhuStatus)
        tvMoistureStatus = view.findViewById(R.id.tvMoistureStatus)

        badgeNotification = view.findViewById(R.id.badge_notification)
        progressHumidity = view.findViewById(R.id.progress_humidity)
        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome)

        ivWeatherIcon3D = view.findViewById(R.id.ivWeatherIcon3D)
        ivWeatherBg = view.findViewById(R.id.ivWeatherBg)
        layoutWeatherBg = view.findViewById(R.id.layoutWeatherBg)
        tvHomeWeatherCondition = view.findViewById(R.id.tvHomeWeatherCondition)

        // Setup Swipe Refresh
        swipeRefreshHome.setOnRefreshListener {
            refreshData()
        }
        swipeRefreshHome.setColorSchemeColors(resources.getColor(R.color.green_primary, null))

        // Cek status notifikasi untuk menampilkan badge jika ada yang belum dibaca
        checkUnreadNotifications()

        view.findViewById<View>(R.id.card_notification).setOnClickListener {
            badgeNotification.visibility = View.GONE
            val bottomSheet = NotificationBottomSheet()
            bottomSheet.show(parentFragmentManager, "NotificationBottomSheet")
        }

        setupHeader()
        setupProfileAndWeather(view)
        setupCharts()
        observeViewModel()

        switchPompa.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                updatePumpStatus(isChecked)
                viewModel.setPompa(isChecked)
            }
        }
    }

    private fun setupCharts() {
        val chartSuhu = view?.findViewById<LineChart>(R.id.chart_suhu)
        val chartKelembapanTanah = view?.findViewById<LineChart>(R.id.chart_kelembapan_tanah)

        val configChart = { chart: LineChart? ->
            chart?.apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(false)
                setDrawGridBackground(false)
                setDrawBorders(false)

                axisLeft.setDrawAxisLine(false)
                axisRight.setDrawAxisLine(false)
                xAxis.setDrawAxisLine(false)

                setNoDataText("Memuat data...")
                setNoDataTextColor(android.graphics.Color.GRAY)
            }
        }

        configChart(chartSuhu)
        configChart(chartKelembapanTanah)
    }

    override fun onResume() {
        super.onResume()
        checkUnreadNotifications()
        updateGreetingName()
    }

    private fun checkUnreadNotifications() {
        if (!isAdded) return

        val sharedPrefs = requireContext().getSharedPreferences("notif_prefs", android.content.Context.MODE_PRIVATE)
        val readIdsSet = sharedPrefs.getStringSet("read_ids", mutableSetOf()) ?: mutableSetOf()

        val databaseRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("notifications")
        databaseRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!isAdded) return

                var hasUnread = false
                if (snapshot.exists()) {
                    for (notifSnapshot in snapshot.children) {
                        val id = notifSnapshot.key ?: continue
                        if (!readIdsSet.contains(id)) {
                            hasUnread = true
                            break
                        }
                    }
                }

                if (hasUnread) {
                    badgeNotification.visibility = View.VISIBLE
                } else {
                    badgeNotification.visibility = View.GONE
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                // Do nothing
            }
        })
    }

    private fun updateGreetingName() {
        val auth = FirebaseAuth.getInstance()
        val userName = auth.currentUser?.displayName?.split(" ")?.get(0) ?: "Pengguna"

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..11 -> "Selamat Pagi"
            in 11..15 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }

        tvGreeting.text = "$greeting, "
        tvUser.text = userName.replaceFirstChar { it.uppercase() }
    }

    private fun setupHeader() {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())
        updateGreetingName()
    }

    private fun observeViewModel() {
        viewModel.sensorData.observe(viewLifecycleOwner) { sensor ->
            // Hide shimmer, show content
            view?.findViewById<View>(R.id.shimmerSuhu)?.visibility = View.GONE
            view?.findViewById<View>(R.id.shimmerTanah)?.visibility = View.GONE
            view?.findViewById<View>(R.id.shimmerUdara)?.visibility = View.GONE
            
            tvSuhu.visibility = View.VISIBLE
            tvKelembapanTanah.visibility = View.VISIBLE
            progressHumidity.visibility = View.VISIBLE
            tvKelembapanUdara.visibility = View.VISIBLE
            
            tvSuhu.text = "${sensor.suhu}°C"
            tvKelembapanTanah.text = "${sensor.kelembapan_persen}%"
            tvKelembapanUdara.text = "${sensor.kelembapan_udara}%"
            progressHumidity.progress = sensor.kelembapan_udara
            tvWeatherHumidity.text = "${sensor.kelembapan_udara}%"

            when {
                sensor.suhu < 20.0 -> {
                    tvSuhuStatus.text = "Dingin"
                    tvSuhuStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
                    tvSuhuStatus.setBackgroundResource(R.drawable.bg_chip_light_blue)
                }
                sensor.suhu > 35.0 -> {
                    tvSuhuStatus.text = "Panas"
                    tvSuhuStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                    tvSuhuStatus.setBackgroundResource(R.drawable.bg_badge_red)
                }
                else -> {
                    tvSuhuStatus.text = "Normal"
                    tvSuhuStatus.setTextColor(android.graphics.Color.parseColor("#10B981"))
                    tvSuhuStatus.setBackgroundResource(R.drawable.bg_chip_light_green)
                }
            }

            when {
                sensor.kelembapan_persen < 30.0 -> {
                    tvMoistureStatus.text = "Kering"
                    tvMoistureStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                    tvMoistureStatus.setBackgroundResource(R.drawable.bg_badge_red)
                }
                sensor.kelembapan_persen > 70.0 -> {
                    tvMoistureStatus.text = "Basah"
                    tvMoistureStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
                    tvMoistureStatus.setBackgroundResource(R.drawable.bg_chip_light_blue)
                }
                else -> {
                    tvMoistureStatus.text = "Normal"
                    tvMoistureStatus.setTextColor(android.graphics.Color.parseColor("#10B981"))
                    tvMoistureStatus.setBackgroundResource(R.drawable.bg_chip_light_green)
                }
            }

        }

        viewModel.pengaturanData.observe(viewLifecycleOwner) { pengaturan ->
            view?.findViewById<TextView>(R.id.tv_suhu_threshold)?.text = "Min: ${pengaturan.suhu_minimum}°"
            view?.findViewById<TextView>(R.id.tv_kelembapan_tanah_threshold)?.text = "Max: ${pengaturan.kelembapan_tanah_maksimum}%"
        }

        viewModel.kontrolData.observe(viewLifecycleOwner) { kontrol ->
            val isOtomatis = kontrol.mode == "otomatis"
            tvModeStatus.text = if (isOtomatis) "Otomatis" else "Manual"
            tvModeStatus.setBackgroundResource(
                if (isOtomatis) R.drawable.bg_chip_light_blue else R.drawable.bg_chip_light_blue
            )
            tvModeStatus.setTextColor(
                resources.getColor(
                    if (isOtomatis) R.color.green_kangkung else R.color.text_dark,
                    null
                )
            )
            switchPompa.isEnabled = !isOtomatis
            switchPompa.alpha = if (isOtomatis) 0.55f else 1f
            if (switchPompa.isChecked != kontrol.pompa) {
                switchPompa.isChecked = kontrol.pompa
            }
            updatePumpStatus(kontrol.pompa)
        }

        viewModel.suhuHistory.observe(viewLifecycleOwner) { history ->
            val chartSuhu = view?.findViewById<LineChart>(R.id.chart_suhu)
            if (history.isNotEmpty() && chartSuhu != null) {
                val entries = history.mapIndexed { index, point -> Entry(index.toFloat(), point.value) }
                val dataSet = LineDataSet(entries, "Suhu")
                dataSet.color = android.graphics.Color.parseColor("#10B981")
                dataSet.setDrawCircles(false)
                dataSet.setDrawValues(false)
                dataSet.lineWidth = 2f
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.setDrawFilled(true)
                dataSet.fillColor = android.graphics.Color.parseColor("#A7F3D0")
                dataSet.fillAlpha = 50

                chartSuhu.data = LineData(dataSet)
                chartSuhu.invalidate()
            }
        }

        viewModel.kelembapanTanahHistory.observe(viewLifecycleOwner) { history ->
            val chartTanah = view?.findViewById<LineChart>(R.id.chart_kelembapan_tanah)
            if (history.isNotEmpty() && chartTanah != null) {
                val entries = history.mapIndexed { index, point -> Entry(index.toFloat(), point.value) }
                val dataSet = LineDataSet(entries, "Kelembapan Tanah")
                dataSet.color = android.graphics.Color.parseColor("#3B82F6")
                dataSet.setDrawCircles(false)
                dataSet.setDrawValues(false)
                dataSet.lineWidth = 2f
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                dataSet.setDrawFilled(true)
                dataSet.fillColor = android.graphics.Color.parseColor("#BFDBFE")
                dataSet.fillAlpha = 50

                chartTanah.data = LineData(dataSet)
                chartTanah.invalidate()
            }
        }
    }

    private fun setupProfileAndWeather(view: View) {
        view.findViewById<View>(R.id.cardWeather)?.setOnClickListener {
            startActivity(Intent(activity, WeatherActivityModern::class.java))
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fetchLocationAndWeather()
    }

    private fun updatePumpStatus(isOn: Boolean) {
        pumpStatusState = isOn
    }

    private fun fetchLocationAndWeather() {
        // Lokasi di-hardcode sesuai permintaan koordinat: 3°47'31.7"S 114°48'28.7"E
        getWeatherSummary("-3.7921389,114.8079722")
    }

    private fun refreshData() {
        swipeRefreshHome.isRefreshing = true
        fetchLocationAndWeather()
        checkUnreadNotifications()
        updateGreetingName()

        // Data Firebase biasanya real-time, tapi kita paksa cek lagi jika perlu
        // Di sini kita kasih delay sebentar agar animasi refresh terlihat
        view?.postDelayed({
            if (isAdded) swipeRefreshHome.isRefreshing = false
        }, 1500)
    }

    private fun getWeatherSummary(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        weatherApi.getForecast(WEATHER_API_KEY, query, 1, "no", "no", "id")
            .enqueue(object : Callback<WeatherApiResponse> {
                override fun onResponse(call: Call<WeatherApiResponse>, response: Response<WeatherApiResponse>) {
                    if (response.isSuccessful && response.body() != null && isAdded) {
                        val data = response.body()!!

                        tvLocation.text = "📍 ${data.location.name}"
                        tvWeatherTemp.text = "${Math.round(data.current.tempC)}°C"

                        val conditionCode = data.current.condition.code
                        val conditionText = com.example.it_project_2.utils.WeatherThemeEngine.translateCondition(data.current.condition.text)
                        val isDay = data.current.isDay
                        tvHomeWeatherCondition.text = conditionText

                        val theme = com.example.it_project_2.utils.WeatherThemeEngine.getTheme(conditionCode, isDay)

                        ivWeatherBg.setImageResource(theme.backgroundResId)
                        ivWeatherIcon3D.setImageResource(theme.iconResId)
                    }
                }
                override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {}
            })
    }
}