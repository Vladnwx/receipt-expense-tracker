package com.qrcode.scanner.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qrcode.scanner.ui.update.UpdateDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(
                    onCheckUpdate = { checkForUpdate() },
                    onBack = { findNavController().navigateUp() }
                )
            }
        }
    }

    private fun checkForUpdate() {
        viewModel.checkUpdate { result ->
            if (result.isAvailable && !result.downloadUrl.isNullOrBlank()) {
                val dialog = UpdateDialogFragment.newInstance(
                    latestVersion = result.latestVersion.orEmpty(),
                    downloadUrl = result.downloadUrl,
                    releaseNotes = result.releaseNotes.orEmpty(),
                    isMandatory = result.isMandatory
                )
                dialog.show(parentFragmentManager, UpdateDialogFragment.TAG)
            }
        }
    }
}
