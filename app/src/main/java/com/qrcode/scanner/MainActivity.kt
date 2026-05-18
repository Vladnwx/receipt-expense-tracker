package com.qrcode.scanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val options = NavOptions.Builder()
                .setPopUpTo(navController.graph.findStartDestination().id, true)
                .setLaunchSingleTop(true)
                .build()
            NavigationUI.onNavDestinationSelected(item, navController, options)
            true
        }

        checkForUpdates()
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
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
        }
    }
}
