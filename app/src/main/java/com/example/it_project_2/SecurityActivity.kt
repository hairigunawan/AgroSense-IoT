package com.example.it_project_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.it_project_2.databinding.ActivitySecurityBinding

class SecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecurityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackSecurity.setOnClickListener {
            finish()
        }
    }
}
