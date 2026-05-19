package com.qrcode.scanner.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qrcode.scanner.R
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
                    viewModel = viewModel,
                    onUpdateAvailable = { latestVersion, downloadUrl, releaseNotes, isMandatory ->
                        val dialog = UpdateDialogFragment.newInstance(
                            latestVersion = latestVersion,
                            downloadUrl = downloadUrl,
                            releaseNotes = releaseNotes,
                            isMandatory = isMandatory
                        )
                        dialog.show(parentFragmentManager, UpdateDialogFragment.TAG)
                    },
                    onAccountsClick = {
                        findNavController().navigate(R.id.action_settings_to_accounts)
                    },
                    onBack = { findNavController().navigateUp() }
                )
            }
        }
    }
}
