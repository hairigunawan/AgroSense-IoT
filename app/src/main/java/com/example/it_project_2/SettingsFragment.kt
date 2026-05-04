package com.example.it_project_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val sliderSuhu = view.findViewById<Slider>(R.id.slider_suhu)
        val tvSuhuValue = view.findViewById<TextView>(R.id.tv_suhu_value)

        val sliderKelembapan = view.findViewById<Slider>(R.id.slider_kelembapan)
        val tvKelembapanValue = view.findViewById<TextView>(R.id.tv_kelembapan_value)

        // Listener untuk slider Suhu
        sliderSuhu.addOnChangeListener { _, value, _ ->
            tvSuhuValue.text = "> ${value.toInt()}°"
        }

        // Listener untuk slider Kelembapan
        sliderKelembapan.addOnChangeListener { _, value, _ ->
            tvKelembapanValue.text = "> ${value.toInt()}%"
        }

        return view
    }
}