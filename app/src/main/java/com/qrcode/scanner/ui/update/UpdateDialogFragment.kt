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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.qrcode.scanner.R
import androidx.compose.material3.Button
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateDialogFragment : DialogFragment() {

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
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val args = requireArguments()
                val isMandatory = args.getBoolean("isMandatory", false)
                latestVersion = args.getString("latestVersion", "")
                downloadUrl = args.getString("downloadUrl", "")
                val releaseNotes = args.getString("releaseNotes", "")

                UpdateDialogContent(
                    latestVersion = latestVersion,
                    releaseNotes = releaseNotes,
                    isMandatory = isMandatory,
                    canInstall = viewModel.canInstall(requireContext()),
                    onUpdate = {
                        if (!viewModel.canInstall(requireContext())) {
                            viewModel.openInstallSettings(requireContext())
                        } else {
                            startDownload()
                        }
                    },
                    onSkip = { dismiss() }
                )
            }
        }
    }

    private fun startDownload() {
        val fileName = "receipt-expense-tracker-$latestVersion.apk"
        downloadId = viewModel.startDownload(requireContext(), downloadUrl, fileName)
        requireContext().registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
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

@Composable
private fun UpdateDialogContent(
    latestVersion: String,
    releaseNotes: String,
    isMandatory: Boolean,
    canInstall: Boolean,
    onUpdate: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Icon(
            Icons.Filled.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isMandatory) "Требуется обновление" else "Доступно обновление",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Версия $latestVersion",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (releaseNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Что нового",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                                .height(120.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onUpdate,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Обновить сейчас")
        }
        if (!canInstall) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Разрешите установку из неизвестных источников в настройках",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (!isMandatory) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Пропустить эту версию")
            }
        }
    }
}
