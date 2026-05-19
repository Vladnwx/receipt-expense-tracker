package com.qrcode.scanner.ui.scanner

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.qrcode.scanner.ui.theme.ScannerControlColors
import kotlinx.coroutines.delay

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    isTorchOn: Boolean,
    pickedImageUri: String?,
    onTorchClick: () -> Unit,
    onCameraSwitchClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClear: () -> Unit,
    onPreviewViewCreated: (PreviewView) -> Unit,
    onNavigateToReceipt: (Long) -> Unit
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.observeAsState(true)
    val isProcessing by viewModel.isProcessing.observeAsState(false)
    val lastQrDetectedMs by viewModel.lastQrDetectedMs.observeAsState(0L)
    val eventWrapper by viewModel.event.observeAsState()

    var loadedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var statusText by remember { mutableStateOf("Ожидание сканирования...") }
    var showScanHint by remember { mutableStateOf(false) }
    var showQrPickerDialog by remember { mutableStateOf(false) }
    var galleryQrList by remember { mutableStateOf<List<String>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pickedImageUri) {
        if (pickedImageUri != null) {
            try {
                val uri = Uri.parse(pickedImageUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                loadedBitmap = inputStream?.use { BitmapFactory.decodeStream(it) }
            } catch (_: Exception) {
                loadedBitmap = null
            }
        } else {
            loadedBitmap = null
        }
    }

    LaunchedEffect(isScanning) {
        if (!isScanning) {
            showScanHint = false
            return@LaunchedEffect
        }
        delay(8000)
        showScanHint = true
    }

    LaunchedEffect(lastQrDetectedMs) {
        if (!isScanning || lastQrDetectedMs == 0L) return@LaunchedEffect
        showScanHint = false
        delay(8000)
        showScanHint = true
    }

    LaunchedEffect(eventWrapper) {
        val event = eventWrapper?.getContentIfNotHandled() ?: return@LaunchedEffect
        when (event) {
            is ScannerEvent.Saved -> {
                statusText = "Сохранено, проверка данных..."
                snackbarHostState.showSnackbar(
                    message = "QR найден, чек сохранён",
                    duration = SnackbarDuration.Short
                )
            }
            is ScannerEvent.AlreadyExists -> {
                statusText = "Чек уже существует"
                val result = snackbarHostState.showSnackbar(
                    message = "Чек уже существует",
                    actionLabel = "Открыть",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onNavigateToReceipt(event.receiptId)
                }
            }
            is ScannerEvent.CheckStarted -> {
                statusText = "Проверка данных..."
                snackbarHostState.showSnackbar(
                    message = "Проверка данных...",
                    duration = SnackbarDuration.Indefinite
                )
            }
            is ScannerEvent.CheckSuccess -> {
                statusText = "Готово: данные получены"
                snackbarHostState.showSnackbar(
                    message = "Данные получены, расходы добавлены",
                    duration = SnackbarDuration.Short
                )
            }
            is ScannerEvent.CheckWarning -> {
                statusText = "Готово: счёт не указан"
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short
                )
            }
            is ScannerEvent.CheckError -> {
                statusText = event.message
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Indefinite
                )
            }
            is ScannerEvent.Error -> {
                statusText = event.message
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short
                )
            }
            is ScannerEvent.GalleryQrList -> {
                galleryQrList = event.qrs
                showQrPickerDialog = true
            }
        }
    }

    if (showQrPickerDialog && galleryQrList.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showQrPickerDialog = false },
            title = { Text("Найдено несколько QR-кодов") },
            text = {
                Column {
                    galleryQrList.forEachIndexed { index, qr ->
                        TextButton(
                            onClick = {
                                showQrPickerDialog = false
                                viewModel.selectGalleryQr(index)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = qr,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrPickerDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                contentAlignment = Alignment.Center
            ) {
                if (pickedImageUri != null && loadedBitmap != null) {
                    Image(
                        bitmap = loadedBitmap!!.asImageBitmap(),
                        contentDescription = "Выбранное изображение",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (isScanning) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { pv ->
                                onPreviewViewCreated(pv)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (!isScanning && pickedImageUri == null) {
                    Text(
                        text = "Камера остановлена",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                if (showScanHint && isScanning && !isProcessing) {
                    Text(
                        text = "Наведите камеру на QR-код",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                if (isScanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Результат сканирования:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Галерея")
                }
                OutlinedButton(
                    onClick = {
                        statusText = "Ожидание сканирования..."
                        loadedBitmap = null
                        onClear()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Очистить")
                }
                Button(
                    onClick = { viewModel.toggleScanning() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isScanning) "Остановить" else "Начать")
                }
            }
        }
    }
}
