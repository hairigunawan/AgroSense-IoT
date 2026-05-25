package com.example.it_project_2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val WEATHER_API_KEY = "440b67ccfc304047819164625260205"

    private lateinit var tvSuhu: TextView
    private lateinit var tvKelembapanTanah: TextView
    private lateinit var tvKelembapanUdara: TextView
    private lateinit var tvStatusPenyiraman: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvWeatherTemp: TextView
    private lateinit var viewStatusDot: View
    private lateinit var switchPompa: SwitchMaterial
    private lateinit var tvModeStatus: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvGreeting: TextView

    private lateinit var tvWeatherHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    
    private lateinit var ivWeatherIcon3D: ImageView
    private lateinit var layoutWeatherBg: View
    private lateinit var tvHomeWeatherCondition: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndWeather()
        } else {
            getWeatherSummary("Ujung Batu")
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
        tvStatusPenyiraman = view.findViewById(R.id.tvStatusPenyiraman)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvWeatherTemp = view.findViewById(R.id.tvWeatherTemp)
        viewStatusDot = view.findViewById(R.id.viewStatusDot)
        switchPompa = view.findViewById(R.id.switchPompa)
        tvModeStatus = view.findViewById(R.id.tvModeStatus)
        tvDate = view.findViewById(R.id.tvDate)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvWeatherHumidity = view.findViewById(R.id.tvWeatherHumidity)
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed)
        
        ivWeatherIcon3D = view.findViewById(R.id.ivWeatherIcon3D)
        layoutWeatherBg = view.findViewById(R.id.layoutWeatherBg)
        tvHomeWeatherCondition = view.findViewById(R.id.tvHomeWeatherCondition)

        view.findViewById<View>(R.id.card_notification).setOnClickListener {
            Toast.makeText(requireContext(), "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
        }

        setupHeader()
        setupProfileAndWeather(view)
        observeViewModel()

        switchPompa.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPompa(isChecked)
        }
    }

    private fun setupHeader() {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvDate.text = sdf.format(Date())

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..11 -> "Selamat Pagi"
            in 12..15 -> "Selamat Siang"
            in 16..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
        
        val auth = FirebaseAuth.getInstance()
        val userName = auth.currentUser?.displayName?.split(" ")?.get(0) ?: "Pengguna"
        tvGreeting.text = "$greeting, $userName"
    }

    private fun observeViewModel() {
        viewModel.sensorData.observe(viewLifecycleOwner) { sensor ->
            tvSuhu.text = "${sensor.suhu}°C"
            tvKelembapanTanah.text = "${sensor.kelembapan_persen}%"
            tvKelembapanUdara.text = "${sensor.kelembapan_udara}%"
            tvWeatherHumidity.text = "${sensor.kelembapan_udara}%"

            if (sensor.status_pompa) {
                tvStatusPenyiraman.text = "Aktif"
                tvStatusPenyiraman.setTextColor(resources.getColor(R.color.green_primary, null))
                viewStatusDot.setBackgroundResource(R.drawable.dot_green)
            } else {
                tvStatusPenyiraman.text = "Mati"
                tvStatusPenyiraman.setTextColor(resources.getColor(R.color.text_dark, null))
                viewStatusDot.setBackgroundResource(android.R.color.darker_gray)
            }
        }

        viewModel.kontrolData.observe(viewLifecycleOwner) { kontrol ->
            val isOtomatis = kontrol.mode == "otomatis"
            tvModeStatus.text = if (isOtomatis) "Otomatis" else "Manual"
            switchPompa.isEnabled = !isOtomatis
            if (switchPompa.isChecked != kontrol.pompa) {
                switchPompa.isChecked = kontrol.pompa
            }
        }
    }

    private fun setupProfileAndWeather(view: View) {
        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile_home)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.photoUrl != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.user)
        }

        ivProfile.setOnClickListener {
            val navView = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            navView?.selectedItemId = R.id.navigation_profile
        }

        view.findViewById<View>(R.id.cardWeather)?.setOnClickListener {
            startActivity(Intent(activity, WeatherActivityModern::class.java))
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fetchLocationAndWeather()
    }

    private fun fetchLocationAndWeather() {
        // Lokasi di-hardcode ke 1 kelurahan/kecamatan sesuai permintaan
        getWeatherSummary("Bajuin, Tanah Laut")
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
                        val conditionText = data.current.condition.text
                        tvHomeWeatherCondition.text = conditionText

                        when {
                            conditionCode == 1000 -> {
                                ivWeatherIcon3D.setImageResource(R.drawable.cerah)
                                layoutWeatherBg.setBackgroundResource(R.drawable.bg_weather_gradient_day)
                            }
                            conditionCode in listOf(1003, 1006, 1009, 1030) -> {
                                ivWeatherIcon3D.setImageResource(R.drawable.cerah)
                                layoutWeatherBg.setBackgroundResource(R.drawable.bg_weather_gradient_day)
                            }
                            conditionCode in listOf(1063, 1180, 1183, 1186, 1189, 1192, 1195) -> {
                                ivWeatherIcon3D.setImageResource(R.drawable.hujan_ringan)
                                layoutWeatherBg.setBackgroundResource(R.drawable.bg_weather_gradient)
                            }
                            conditionCode in listOf(1087, 1273, 1276, 1279, 1282) -> {
                                ivWeatherIcon3D.setImageResource(R.drawable.hujan_badai)
                                layoutWeatherBg.setBackgroundResource(R.drawable.bg_weather_gradient)
                            }
                            else -> {
                                ivWeatherIcon3D.setImageResource(R.drawable.cerah)
                                layoutWeatherBg.setBackgroundResource(R.drawable.bg_weather_gradient_day)
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {}
            })
    }
}
