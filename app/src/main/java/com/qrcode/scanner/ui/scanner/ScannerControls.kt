package com.qrcode.scanner.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.ui.theme.ScannerControlColors

@Composable
fun ScannerControls(
    isTorchOn: Boolean,
    onTorchClick: () -> Unit,
    onCameraSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        FilledIconButton(
            onClick = onTorchClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = ScannerControlColors.container
            )
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                contentDescription = if (isTorchOn) "Выключить фонарик" else "Включить фонарик",
                tint = ScannerControlColors.icon
            )
        }

        FilledIconButton(
            onClick = onCameraSwitchClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = ScannerControlColors.container
            )
        ) {
            Icon(
                imageVector = Icons.Filled.FlipCameraAndroid,
                contentDescription = "Переключить камеру",
                tint = ScannerControlColors.icon
            )
        }
    }
}
