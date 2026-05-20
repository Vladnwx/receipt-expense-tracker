package com.qrcode.scanner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.qrcode.scanner.data.reporter.AppLogger
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

            handleShareIntent(intent)
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "onCreate failed", e)
            val tv = TextView(this).apply {
                text = "Ошибка: ${e.localizedMessage}"
                setTextColor(0xFFFF0000.toInt())
                textSize = 18f
            }
            setContentView(tv)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val uri = when {
            intent.type?.startsWith("application/json") == true ->
                intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
            intent.type?.startsWith("text/plain") == true -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    navigateToScannerWithJson(text)
                    return
                }
                null
            }
            else -> null
        }
        if (uri != null) {
            try {
                val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (json != null) navigateToScannerWithJson(json)
            } catch (e: Exception) {
                AppLogger.e("MainActivity", "Failed to read shared JSON", e)
            }
        }
    }

    private fun navigateToScannerWithJson(json: String) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bundle = Bundle().apply {
            putString("shared_fns_json", json)
        }
        if (navController.currentDestination?.id != R.id.scannerFragment) {
            navController.navigate(R.id.scannerFragment, bundle)
        }
    }
}
