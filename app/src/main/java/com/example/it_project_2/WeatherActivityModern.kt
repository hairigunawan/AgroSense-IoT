package com.example.it_project_2

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.it_project_2.databinding.ActivityWeatherModernBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class WeatherActivityModern : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherModernBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val WEATHER_API_KEY = "440b67ccfc304047819164625260205"
    private var rainAnimator: ValueAnimator? = null

    private var snowAnimator: ValueAnimator? = null
    private var stormAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make edge-to-edge
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }

        binding = ActivityWeatherModernBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Setup empty adapter initially
        binding.rvHourlyModern.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvHourlyModern.adapter = HourlyModernAdapter(emptyList())

        // Back button action (Using header as a fallback)
        binding.llHeader.setOnClickListener {
            finish()
        }

        setupAnimations()
        fetchLocationAndWeather()
    }

    private fun setupAnimations() {
        // 1. Glow breathing animation (Alpha fade)
        ObjectAnimator.ofFloat(binding.vGlow, "alpha", 0.05f, 0.2f).apply {
            duration = 4000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        // 2. Clouds moving slowly (Translate animation)
        ObjectAnimator.ofFloat(binding.ivCloud1, "translationX", -20f, 30f).apply {
            duration = 15000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        
        ObjectAnimator.ofFloat(binding.ivCloud2, "translationX", 20f, -30f).apply {
            duration = 18000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        
        // 3. Scale pulsing on clouds for a dynamic effect
        ObjectAnimator.ofFloat(binding.ivCloud1, "scaleX", 1.0f, 1.05f).apply {
            duration = 8000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        ObjectAnimator.ofFloat(binding.ivCloud1, "scaleY", 1.0f, 1.05f).apply {
            duration = 8000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        // 4. Rain Particles (Falling & Fading)
        rainAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                binding.flRain.translationY = progress * 300f
                binding.flRain.alpha = if (progress < 0.2f) progress * 5f else if (progress > 0.8f) (1f - progress) * 5f else 1f
            }
        }
        
        // 5. Snow Particles (Falling & Swaying)
        snowAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                binding.flSnow.translationY = progress * 200f
                binding.flSnow.translationX = (Math.sin(progress * Math.PI * 2) * 50).toFloat()
                binding.flSnow.alpha = if (progress < 0.2f) progress * 5f else if (progress > 0.8f) (1f - progress) * 5f else 1f
            }
        }

        // 6. Storm Flash
        stormAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 5000
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                if (progress in 0.9f..0.95f) {
                     binding.vStormFlash.visibility = View.VISIBLE
                     binding.vStormFlash.alpha = (0.95f - progress) * 10f // quick flash
                } else {
                     binding.vStormFlash.visibility = View.GONE
                }
            }
        }

        // Parallax effect on scroll
        binding.scrollContent.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.vGlow.translationY = scrollY * 0.5f
            binding.ivCloud1.translationY = scrollY * 0.3f
            binding.ivCloud2.translationY = scrollY * 0.4f
        }
    }

    private fun fetchLocationAndWeather() {
        // Lokasi di-hardcode sesuai permintaan koordinat: 3°47'31.7"S 114°48'28.7"E
        fetchWeatherData("-3.7921389,114.8079722")
    }

    private fun fetchWeatherData(query: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherApi::class.java)
        val call = weatherApi.getForecast(WEATHER_API_KEY, query, 3, "no", "no", "id")

        call.enqueue(object : Callback<WeatherApiResponse> {
            override fun onResponse(call: Call<WeatherApiResponse>, response: Response<WeatherApiResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    updateUI(response.body()!!)
                } else {
                    Toast.makeText(this@WeatherActivityModern, "Gagal mengambil data cuaca", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherApiResponse>, t: Throwable) {
                Toast.makeText(this@WeatherActivityModern, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyDynamicTheme(conditionCode: Int, isDay: Int) {
        val theme = com.example.it_project_2.utils.WeatherThemeEngine.getTheme(conditionCode, isDay)
        
        // 1. Background
        binding.root.setBackgroundResource(theme.backgroundResId)
        
        // 2. Glow adaptation
        if (theme.isDarkTheme) {
             binding.vGlow.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#44FFFFFF"))
        } else {
             binding.vGlow.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#88FFFFFF"))
        }

        // 3. Particles
        if (theme.isRaining && theme.isStorming) {
            binding.flRain.visibility = View.VISIBLE
            binding.flSnow.visibility = View.GONE
            rainAnimator?.start()
            snowAnimator?.cancel()
            stormAnimator?.start()
        } else if (theme.isRaining) {
            binding.flRain.visibility = View.VISIBLE
            binding.flSnow.visibility = View.GONE
            binding.vStormFlash.visibility = View.GONE
            rainAnimator?.start()
            snowAnimator?.cancel()
            stormAnimator?.cancel()
        } else if (conditionCode in listOf(1066, 1114, 1213, 1219, 1225, 1258)) { // Snow conditions
            binding.flRain.visibility = View.GONE
            binding.flSnow.visibility = View.VISIBLE
            binding.vStormFlash.visibility = View.GONE
            rainAnimator?.cancel()
            snowAnimator?.start()
            stormAnimator?.cancel()
        } else {
            binding.flRain.visibility = View.GONE
            binding.flSnow.visibility = View.GONE
            binding.vStormFlash.visibility = View.GONE
            rainAnimator?.cancel()
            snowAnimator?.cancel()
            stormAnimator?.cancel()
        }
        
        // Clouds adjustments based on theme
        if (theme.isDarkTheme) {
            binding.ivCloud1.alpha = 0.15f
            binding.ivCloud2.alpha = 0.10f
        } else {
            binding.ivCloud1.alpha = 0.4f
            binding.ivCloud2.alpha = 0.25f
        }
    }

    private fun updateUI(data: WeatherApiResponse) {
        // Apply Adaptive Theme
        applyDynamicTheme(data.current.condition.code, data.current.isDay)

        // Location
        binding.tvLocationName.text = data.location.name
        
        // Current Weather
        val currentTemp = data.current.tempC.roundToInt()
        binding.tvMainTemp.text = "$currentTemp°"
        binding.tvCondition.text = data.current.condition.text
        
        // High/Low and Feels Like
        val todayForecast = data.forecast.forecastDay.firstOrNull()
        if (todayForecast != null) {
            val max = todayForecast.day.maxTempC.roundToInt()
            val min = todayForecast.day.minTempC.roundToInt()
            binding.tvHighLow.text = "T: $max°  R: $min°"
            
            // Rain Prediction Card update (Find max rain chance for today)
            val maxRainChance = todayForecast.hour.maxOfOrNull { it.chanceOfRain } ?: 0
            binding.tvRainPercent.text = "$maxRainChance%"
            if (maxRainChance > 0) {
                binding.tvRainTitle.text = "Potensi Presipitasi"
                binding.tvRainSubtitle.text = "Peluang presipitasi tertinggi hari ini"
            } else {
                binding.tvRainTitle.text = "Cuaca Cerah"
                binding.tvRainSubtitle.text = "Tidak ada potensi hujan"
            }
            
            // Hourly Forecast update
            val hourlyList = todayForecast.hour.map { hourData ->
                HourlyModern(
                    hour = hourData.time, // This uses the custom getter which returns HH:mm
                    temp = "${hourData.tempC.roundToInt()}°",
                    iconUrl = hourData.condition.icon,
                    rainChance = "${hourData.chanceOfRain}%"
                )
            }
            binding.rvHourlyModern.adapter = HourlyModernAdapter(hourlyList)
        }
        
        val feelsLike = data.current.feelsLikeC.roundToInt()
        binding.tvFeelsLike.text = "Terasa seperti $feelsLike°"
        
        // Humidity
        val humidity = data.current.humidity
        binding.tvHumidityPercent.text = "$humidity%"
        
        // Description
        binding.tvDescription.text = "Kondisi saat ini: ${data.current.condition.text} dengan suhu $currentTemp°C."

        // Daily Forecast update
        binding.llDailyContainer.removeAllViews()
        val dateFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale("id", "ID"))
        val dateParser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        data.forecast.forecastDay.forEach { dailyData ->
            val dayView = layoutInflater.inflate(R.layout.item_daily_modern, binding.llDailyContainer, false)
            
            val tvDay = dayView.findViewById<android.widget.TextView>(R.id.tvDay)
            val ivDailyIcon = dayView.findViewById<android.widget.ImageView>(R.id.ivDailyIcon)
            val tvDailyRain = dayView.findViewById<android.widget.TextView>(R.id.tvDailyRain)
            val tvDailyTempRange = dayView.findViewById<android.widget.TextView>(R.id.tvDailyTempRange)

            if (dailyData.date != null) {
                try {
                    val date = dateParser.parse(dailyData.date)
                    if (date != null) {
                        tvDay.text = dateFormat.format(date)
                    } else {
                        tvDay.text = "Hari"
                    }
                } catch (e: Exception) {
                    tvDay.text = "Hari"
                }
            }

            val dayInfo = dailyData.day
            if (dayInfo != null) {
                tvDailyRain.text = "${dayInfo.dailyChanceOfRain}%"
                tvDailyTempRange.text = "${dayInfo.maxTempC.roundToInt()}° / ${dayInfo.minTempC.roundToInt()}°"
                
                if (dayInfo.condition != null && dayInfo.condition.icon != null) {
                    com.bumptech.glide.Glide.with(this@WeatherActivityModern)
                        .load(dayInfo.condition.icon)
                        .into(ivDailyIcon)
                }
            }

            binding.llDailyContainer.addView(dayView)
        }
    }
}
