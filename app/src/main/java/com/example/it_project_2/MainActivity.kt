package com.example.it_project_2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.it_project_2.service.DatabaseNotificationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity dengan navigasi navbar black capsule sederhana
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }

    private enum class NavTab { HOME, SENSOR, HISTORY, SETTINGS }

    private lateinit var navBarContainer: LinearLayout
    private lateinit var llNoInternet: LinearLayout
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private lateinit var navHomeContainer: LinearLayout
    private lateinit var navSensorContainer: LinearLayout
    private lateinit var navHistoryContainer: LinearLayout
    private lateinit var navSettingsContainer: LinearLayout

    private lateinit var navHome: ImageButton
    private lateinit var navSensor: ImageButton
    private lateinit var navHistory: ImageButton
    private lateinit var navSettings: ImageButton

    private lateinit var navHomeLabel: TextView
    private lateinit var navSensorLabel: TextView
    private lateinit var navHistoryLabel: TextView
    private lateinit var navSettingsLabel: TextView

    private var currentTab: NavTab = NavTab.HOME
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }

        val serviceIntent = Intent(this, DatabaseNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        llNoInternet = findViewById(R.id.ll_no_internet)
        navBarContainer = findViewById(R.id.nav_bar_container)

        navHomeContainer = findViewById(R.id.nav_home_container)
        navSensorContainer = findViewById(R.id.nav_sensor_container)
        navHistoryContainer = findViewById(R.id.nav_history_container)
        navSettingsContainer = findViewById(R.id.nav_settings_container)

        navHome = findViewById(R.id.nav_home)
        navSensor = findViewById(R.id.nav_sensor)
        navHistory = findViewById(R.id.nav_history)
        navSettings = findViewById(R.id.nav_settings)

        navHomeLabel = findViewById(R.id.nav_home_label)
        navSensorLabel = findViewById(R.id.nav_sensor_label)
        navHistoryLabel = findViewById(R.id.nav_history_label)
        navSettingsLabel = findViewById(R.id.nav_settings_label)

        // Add listeners to containers for better hit area
        navHomeContainer.setOnClickListener { selectTab(NavTab.HOME) }
        navSensorContainer.setOnClickListener { selectTab(NavTab.SENSOR) }
        navHistoryContainer.setOnClickListener { selectTab(NavTab.HISTORY) }
        navSettingsContainer.setOnClickListener { selectTab(NavTab.SETTINGS) }
        
        // Ensure buttons don't consume clicks if container handles them
        navHome.isClickable = false
        navSensor.isClickable = false
        navHistory.isClickable = false
        navSettings.isClickable = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Adjusted navbar height
            val navbarTotalHeight = (80 * resources.displayMetrics.density).toInt()
            v.setPadding(0, insets.top, 0, insets.bottom + navbarTotalHeight)
            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(llNoInternet) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, insets.top, v.paddingRight, v.paddingBottom)
            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(navBarContainer) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val mlp = v.layoutParams as ViewGroup.MarginLayoutParams
            val baseMarginBottom = (20 * resources.displayMetrics.density).toInt()
            mlp.bottomMargin = insets.bottom + baseMarginBottom
            v.layoutParams = mlp
            WindowInsetsCompat.CONSUMED
        }

        if (savedInstanceState == null) {
            selectTab(NavTab.HOME)
        } else {
            updateNavIndicator(currentTab)
        }

        // Animasi siap digunakan setelah initial load
        isInitialLoad = false

        setupNetworkMonitoring()
    }

    fun navigateToHomeTab() {
        if (currentTab != NavTab.HOME) {
            selectTab(NavTab.HOME)
        }
    }

    private fun selectTab(tab: NavTab) {
        currentTab = tab
        showFragment(tab)
        updateNavIndicator(tab)
    }

    private fun updateNavIndicator(activeTab: NavTab) {
        val transition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(Fade())
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
        
        TransitionManager.beginDelayedTransition(navBarContainer, transition)
        val allContainers = listOf(navHomeContainer, navSensorContainer, navHistoryContainer, navSettingsContainer)
        val allIcons = listOf(navHome, navSensor, navHistory, navSettings)
        val allLabels = listOf(navHomeLabel, navSensorLabel, navHistoryLabel, navSettingsLabel)

        for (i in 0 until 4) {
            val tab = NavTab.values()[i]
            val container = allContainers[i]
            val icon = allIcons[i]
            val label = allLabels[i]
            val isActive = tab == activeTab
            
            // Set background capsule only for active
            container.background = if (isActive) ContextCompat.getDrawable(this, R.drawable.bg_active_nav) else null
            
            // Set icon color
            icon.setColorFilter(
                if (isActive) ContextCompat.getColor(this, R.color.text_dark) 
                else Color.WHITE
            )
            
            // Toggle Label visibility
            label.visibility = if (isActive) View.VISIBLE else View.GONE
            label.setTextColor(if (isActive) ContextCompat.getColor(this, R.color.text_dark) else Color.WHITE)
        }
    }

    private fun setupNetworkMonitoring() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!isConnected) {
            llNoInternet.visibility = View.VISIBLE
        }
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread { llNoInternet.visibility = View.GONE }
            }
            override fun onLost(network: Network) {
                runOnUiThread { llNoInternet.visibility = View.VISIBLE }
            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::connectivityManager.isInitialized && ::networkCallback.isInitialized) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private fun showFragment(tab: NavTab) {
        val tag = tab.name
        val fragmentManager = supportFragmentManager
        val targetFragment = fragmentManager.findFragmentByTag(tag) ?: createFragment(tab)
        val transaction = fragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
        fragmentManager.fragments.forEach { fragment ->
            if (fragment.isAdded) transaction.hide(fragment)
        }
        if (targetFragment.isAdded) {
            transaction.show(targetFragment)
        } else {
            transaction.add(R.id.fragment_container, targetFragment, tag)
        }
        transaction.commit()
    }

    private fun createFragment(tab: NavTab): Fragment = when (tab) {
        NavTab.HOME -> HomeFragment()
        NavTab.SENSOR -> SensorFragment()
        NavTab.HISTORY -> HistoryFragment()
        NavTab.SETTINGS -> SettingFragment()
    }
}
