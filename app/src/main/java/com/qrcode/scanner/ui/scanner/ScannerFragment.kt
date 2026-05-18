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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.R
import com.qrcode.scanner.databinding.FragmentScannerBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScannerViewModel by viewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val barcodeScanner = BarcodeScanning.getClient()

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
            viewModel.onImagePicked(uri.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeEvents()
        checkPermission()
    }

    private fun setupUI() {
        binding.scanButton.setOnClickListener { toggleCamera() }
        binding.clearButton.setOnClickListener {
            binding.resultText.text = getString(R.string.result_placeholder)
        }
        binding.galleryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.torchButton.setOnClickListener { toggleTorch() }
        binding.cameraSwitchButton.setOnClickListener { switchCamera() }

        binding.torchButton.isEnabled = false
        binding.cameraSwitchButton.isEnabled = false
    }

    private fun toggleCamera() {
        if (cameraProvider == null) {
            startCamera()
            return
        }
        viewModel.toggleScanning()
    }

    private fun updateUiForScanningState(isScanning: Boolean) {
        binding.scanButton.text = if (isScanning) {
            getString(R.string.stop_scanning)
        } else {
            getString(R.string.start_scanning)
        }

        if (isScanning) {
            showPreview()
            bindCamera()
        } else {
            unbindCamera()
            hidePreview()
        }

        binding.torchButton.isEnabled = isScanning
        binding.cameraSwitchButton.isEnabled = isScanning
        if (!isScanning && isTorchOn) {
            disableTorch()
        }
    }

    private fun showPreview() {
        binding.previewView.visibility = View.VISIBLE
        binding.cameraOverlay.visibility = View.GONE
    }

    private fun hidePreview() {
        binding.previewView.visibility = View.GONE
        binding.cameraOverlay.visibility = View.VISIBLE
    }

    private fun toggleTorch() {
        if (viewModel.isScanning.value != true) return
        if (lensFacing != CameraSelector.LENS_FACING_BACK) return

        isTorchOn = !isTorchOn
        camera?.cameraControl?.enableTorch(isTorchOn)
        updateTorchButton()
    }

    private fun disableTorch() {
        isTorchOn = false
        camera?.cameraControl?.enableTorch(false)
        updateTorchButton()
    }

    private fun updateTorchButton() {
        binding.torchButton.alpha = if (isTorchOn) 1.0f else 0.6f
    }

    private fun switchCamera() {
        if (viewModel.isScanning.value != true) return
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        if (isTorchOn) disableTorch()
        bindCamera()
    }

    private fun observeEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            val scannerEvent = event.getContentIfNotHandled() ?: return@observe
            when (scannerEvent) {
                is ScannerEvent.Saving -> {
                    binding.resultText.text = getString(R.string.saving_receipt)
                }
                is ScannerEvent.QrFound -> {
                    binding.resultText.text = scannerEvent.rawData
                    Toast.makeText(requireContext(), R.string.qr_found, Toast.LENGTH_SHORT).show()
                }
                is ScannerEvent.Parsed -> {
                    val bundle = Bundle().apply { putLong("receiptId", scannerEvent.receiptId) }
                    findNavController().navigate(R.id.action_scanner_to_receiptDetail, bundle)
                }
                is ScannerEvent.AlreadyExists -> {
                    Snackbar.make(binding.root, R.string.receipt_already_exists, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open) {
                            val bundle = Bundle().apply { putLong("receiptId", scannerEvent.receiptId) }
                            findNavController().navigate(R.id.action_scanner_to_receiptDetail, bundle)
                        }
                        .show()
                }
                is ScannerEvent.Error -> {
                    Toast.makeText(requireContext(), R.string.parse_error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.isScanning.observe(viewLifecycleOwner) { isScanning ->
            updateUiForScanningState(isScanning == true)
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showPermissionDenied() {
        Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_LONG).show()
        binding.resultText.text = getString(R.string.permission_required)
        binding.scanButton.isEnabled = false
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera()
                binding.torchButton.isEnabled = true
                binding.cameraSwitchButton.isEnabled = true
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.camera_error, Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCamera() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

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

    private fun processImage(imageProxy: ImageProxy) {
        if (viewModel.isScanning.value != true) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
}
