package com.qrcode.scanner.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.R
import com.qrcode.scanner.data.reporter.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private val viewModel: ScannerViewModel by viewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private var barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner? = null

    private var previewView: PreviewView? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isTorchOn = false
    @Volatile
    private var isActive = false
    private var lastQrDetectedTime = 0L
    private val qrDebounceMs = 2000L

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var composeIsScanning by mutableStateOf(true)
    private var composeTorchOn by mutableStateOf(false)
    private var composeHasPermission by mutableStateOf(false)
    private var composePickedImageUri by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera()
            else composeHasPermission = false
        }
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                composePickedImageUri = uri.toString()
                viewModel.onImagePicked(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return try {
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    ScannerScreen(
                        viewModel = viewModel,
                        isTorchOn = composeTorchOn,
                        pickedImageUri = composePickedImageUri,
                        onTorchClick = { toggleTorch() },
                        onCameraSwitchClick = { switchCamera() },
                        onGalleryClick = { pickImageLauncher.launch("image/*") },
                        onClear = {
                            composePickedImageUri = null
                            if (!composeIsScanning) {
                                viewModel.toggleScanning()
                            }
                        },
                        onPreviewViewCreated = { pv -> previewView = pv },
                        onNavigateToReceipt = { receiptId ->
                            val bundle = Bundle().apply { putLong("receiptId", receiptId) }
                            findNavController().navigate(R.id.action_scanner_to_receiptDetail, bundle)
                        },
                        onNavigateToReceiptList = {
                            findNavController().navigate(R.id.receiptListFragment)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ScannerFrag", "ComposeView creation failed", e)
            inflater.inflate(android.R.layout.simple_list_item_1, container, false).apply {
                findViewById<android.widget.TextView>(android.R.id.text1).text =
                    "Ошибка: ${e.localizedMessage}"
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        isActive = composeIsScanning
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
        cameraExecutor.shutdown()
        barcodeScanner?.close()
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> {
                composeHasPermission = true
                startCamera()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera()
            } catch (e: Exception) {
                AppLogger.e("ScannerFrag", "Camera start error", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCamera() {
        if (!composeIsScanning) return
        val pv = previewView ?: return
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(pv.surfaceProvider) }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        try {
            camera = provider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            AppLogger.e("ScannerFrag", "Bind camera error", e)
        }
    }

    private fun toggleTorch() {
        if (!composeIsScanning) return
        if (lensFacing != CameraSelector.LENS_FACING_BACK) return
        isTorchOn = !isTorchOn
        camera?.cameraControl?.enableTorch(isTorchOn)
        composeTorchOn = isTorchOn
    }

    private fun disableTorch() {
        isTorchOn = false
        camera?.cameraControl?.enableTorch(false)
        composeTorchOn = false
    }

    private fun switchCamera() {
        if (!composeIsScanning) return
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        if (isTorchOn) disableTorch()
        bindCamera()
    }

    private fun processImage(imageProxy: ImageProxy) {
        if (!isActive || viewModel.isProcessingNow) {
            imageProxy.close()
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastQrDetectedTime < qrDebounceMs) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = barcodeScanner ?: BarcodeScanning.getClient().also { barcodeScanner = it }
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val qr = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                val rawValue = qr?.rawValue
                if (rawValue != null && rawValue.isNotBlank()) {
                    lastQrDetectedTime = System.currentTimeMillis()
                    viewModel.onQrDetected(rawValue)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
