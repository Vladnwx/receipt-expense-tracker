package com.qrcode.scanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.qrcode.scanner.data.repository.AppUpdateRepository
import com.qrcode.scanner.databinding.ActivityMainBinding
import com.qrcode.scanner.ui.update.UpdateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

            checkForUpdates()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "onCreate failed", e)
            val tv = android.widget.TextView(this).apply {
                text = "Ошибка: ${e.localizedMessage}"
                setTextColor(0xFFFF0000.toInt())
                textSize = 18f
            }
            setContentView(tv)
        }
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
            try {
                val result = updateRepository.checkForUpdate()
                if (result.isAvailable && !result.downloadUrl.isNullOrBlank()) {
                    val dialog = UpdateDialogFragment.newInstance(
                        latestVersion = result.latestVersion.orEmpty(),
                        downloadUrl = result.downloadUrl,
                        releaseNotes = result.releaseNotes.orEmpty(),
                        isMandatory = result.isMandatory
                    )
                    dialog.show(supportFragmentManager, UpdateDialogFragment.TAG)
                }
            } catch (_: Exception) {
                // fail silently – api error is not critical on startup
            }
        }
    }
}
