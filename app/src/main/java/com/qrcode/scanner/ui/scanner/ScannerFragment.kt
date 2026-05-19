package com.qrcode.scanner.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private val viewModel: ScannerViewModel by viewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val barcodeScanner = BarcodeScanning.getClient()

    private var previewView: PreviewView? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var isTorchOn = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else showPermissionDenied()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            composePickedImageUri = uri.toString()
            viewModel.onImagePicked(uri.toString())
        }
    }

    private var composeIsScanning by mutableStateOf(true)
    private var composeTorchOn by mutableStateOf(false)
    private var composeHasPermission by mutableStateOf(false)
    private var composeResultText by mutableStateOf(getString(R.string.result_placeholder))
    private var composePickedImageUri by mutableStateOf<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return try {
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    androidx.compose.runtime.SideEffect {
                        android.util.Log.d("ScannerFrag", "Composition started")
                    }
                    ScannerScreen(
                        isScanning = composeIsScanning,
                        isTorchOn = composeTorchOn,
                        hasPermission = composeHasPermission,
                        pickedImageUri = composePickedImageUri,
                        resultText = composeResultText,
                        onToggleScan = {
                            composePickedImageUri = null
                            viewModel.toggleScanning()
                            bindCamera()
                        },
                        onTorchClick = { toggleTorch() },
                        onCameraSwitchClick = { switchCamera() },
                        onGalleryClick = { pickImageLauncher.launch("image/*") },
                        onClear = {
                            composeResultText = getString(R.string.result_placeholder)
                            composePickedImageUri = null
                        },
                        onPreviewViewCreated = { pv -> previewView = pv }
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ScannerFrag", "ComposeView creation failed", e)
            inflater.inflate(android.R.layout.simple_list_item_1, container, false).apply {
                findViewById<android.widget.TextView>(android.R.id.text1).text =
                    "Ошибка: ${e.localizedMessage}"
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEvents()
        checkPermission()
    }

    private fun observeEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            val scannerEvent = event.getContentIfNotHandled() ?: return@observe
            when (scannerEvent) {
                is ScannerEvent.Saving -> {
                    composeResultText = getString(R.string.saving_receipt)
                }
                is ScannerEvent.QrFound -> {
                    composeResultText = scannerEvent.rawData
                    Toast.makeText(requireContext(), R.string.qr_found, Toast.LENGTH_SHORT).show()
                }
                is ScannerEvent.Parsed -> {
                    Toast.makeText(requireContext(), R.string.receipt_saved, Toast.LENGTH_SHORT).show()
                }
                is ScannerEvent.AlreadyExists -> {
                    Snackbar.make(requireView(), R.string.receipt_already_exists, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open) {
                            val bundle = Bundle().apply { putLong("receiptId", scannerEvent.receiptId) }
                            findNavController().navigate(R.id.action_scanner_to_receiptDetail, bundle)
                        }
                        .show()
                }
                is ScannerEvent.Error -> {
                    Snackbar.make(requireView(), scannerEvent.message, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }

        viewModel.isScanning.observe(viewLifecycleOwner) { scanning ->
            composeIsScanning = scanning == true
            if (scanning == true) {
                showPreview()
                bindCamera()
            } else {
                unbindCamera()
                hidePreview()
            }
            if (!composeIsScanning && isTorchOn) disableTorch()
        }
    }

    private fun showPreview() {
        previewView?.visibility = View.VISIBLE
    }

    private fun hidePreview() {
        // handled by isScanning state in Compose
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

    private fun showPermissionDenied() {
        composeHasPermission = false
        composeResultText = getString(R.string.permission_required)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.camera_error, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), R.string.camera_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun unbindCamera() {
        cameraProvider?.unbindAll()
        camera = null
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
        if (!composeIsScanning || viewModel.isProcessingNow) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val rawValue = barcodes.first().rawValue
                    if (!rawValue.isNullOrBlank()) {
                        viewModel.onQrDetected(rawValue)
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    override fun onResume() {
        super.onResume()
        if (composeIsScanning && cameraProvider != null) {
            bindCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        unbindCamera()
        if (isTorchOn) disableTorch()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
}
