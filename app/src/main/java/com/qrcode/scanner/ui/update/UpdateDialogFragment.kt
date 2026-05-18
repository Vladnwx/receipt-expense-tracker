package com.qrcode.scanner.ui.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.qrcode.scanner.R
import com.qrcode.scanner.databinding.FragmentUpdateDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateDialogFragment : DialogFragment() {

    private var _binding: FragmentUpdateDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UpdateViewModel by viewModels()

    private var downloadId: Long = -1L
    private var downloadUrl: String = ""
    private var latestVersion: String = ""
    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id == downloadId) {
                viewModel.onDownloadComplete(context, latestVersion)
                try { context.unregisterReceiver(this) } catch (_: Exception) {}
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        val args = requireArguments()
        val isMandatory = args.getBoolean("isMandatory", false)
        latestVersion = args.getString("latestVersion", "")
        downloadUrl = args.getString("downloadUrl", "")
        val releaseNotes = args.getString("releaseNotes", "")

        binding.versionText.text = getString(R.string.version_label, latestVersion)
        binding.releaseNotesText.text = releaseNotes.ifBlank {
            getString(R.string.update_no_notes)
        }

        if (isMandatory) {
            binding.updateTitle.text = getString(R.string.update_required_title)
            binding.skipButton.visibility = View.GONE
        } else {
            binding.updateTitle.text = getString(R.string.update_available_title)
            binding.skipButton.visibility = View.VISIBLE
        }

        checkInstallPermission()

        binding.updateButton.setOnClickListener {
            if (!viewModel.canInstall(requireContext())) {
                viewModel.openInstallSettings(requireContext())
                binding.installPermissionHint.visibility = View.VISIBLE
                return@setOnClickListener
            }
            startDownload()
        }

        binding.skipButton.setOnClickListener {
            dismiss()
        }
    }

    private fun checkInstallPermission() {
        if (!viewModel.canInstall(requireContext())) {
            binding.installPermissionHint.visibility = View.VISIBLE
        }
    }

    private fun startDownload() {
        binding.updateButton.isEnabled = false
        binding.updateButton.text = getString(R.string.update_downloading)
        binding.skipButton.isEnabled = false
        binding.installPermissionHint.visibility = View.GONE

        val fileName = "receipt-expense-tracker-$latestVersion.apk"
        downloadId = viewModel.startDownload(requireContext(), downloadUrl, fileName)

        requireContext().registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.Theme_QRCodeScanner_Dialog

    companion object {
        const val TAG = "UpdateDialogFragment"

        fun newInstance(
            latestVersion: String,
            downloadUrl: String,
            releaseNotes: String,
            isMandatory: Boolean
        ) = UpdateDialogFragment().apply {
            arguments = Bundle().apply {
                putString("latestVersion", latestVersion)
                putString("downloadUrl", downloadUrl)
                putString("releaseNotes", releaseNotes)
                putBoolean("isMandatory", isMandatory)
            }
        }
    }
}
