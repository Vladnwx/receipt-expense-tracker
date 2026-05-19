package com.qrcode.scanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.qrcode.scanner.data.repository.AppUpdateRepository
import com.qrcode.scanner.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var updateRepository: AppUpdateRepository

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "onCreate failed", e)
            val tv = TextView(this).apply {
                text = "Ошибка: ${e.localizedMessage}"
                setTextColor(0xFFFF0000.toInt())
                textSize = 18f
            }
            setContentView(tv)
        }
    }
}
