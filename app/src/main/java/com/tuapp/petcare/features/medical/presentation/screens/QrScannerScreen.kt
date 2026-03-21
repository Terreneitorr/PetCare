package com.tuapp.petcare.features.medical.presentation.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.tuapp.petcare.features.medical.presentation.viewmodels.MedHistoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    petId: String,
    onBack: () -> Unit,
    viewModel: MedHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    var scannedOnce by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cameraPermission.status.isGranted) {

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val executor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val barcodeScanner = BarcodeScanning.getClient()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(executor) { imageProxy ->
                                        if (!scannedOnce) {
                                            @androidx.camera.core.ExperimentalGetImage
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                val image = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                                barcodeScanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        barcodes.firstOrNull { barcode ->
                                                            if (barcode.format != Barcode.FORMAT_QR_CODE) return@firstOrNull false

                                                            // ── Verificar que el QR esté dentro del recuadro ──
                                                            val box = barcode.boundingBox ?: return@firstOrNull true
                                                            val imgW = imageProxy.width.toFloat()
                                                            val imgH = imageProxy.height.toFloat()

                                                            // El recuadro ocupa ~60% del centro de la pantalla
                                                            val margin = 0.20f
                                                            val zoneLeft   = imgW * margin
                                                            val zoneTop    = imgH * margin
                                                            val zoneRight  = imgW * (1f - margin)
                                                            val zoneBottom = imgH * (1f - margin)

                                                            // El QR debe estar completamente dentro de la zona
                                                            box.left   >= zoneLeft  &&
                                                                    box.top    >= zoneTop   &&
                                                                    box.right  <= zoneRight &&
                                                                    box.bottom <= zoneBottom
                                                        }
                                                            ?.rawValue
                                                            ?.let { qrContent ->
                                                                if (!scannedOnce) {
                                                                    scannedOnce = true
                                                                    viewModel.onQrScanned(qrContent)
                                                                    Log.d("QrScanner", "QR detectado: $qrContent")
                                                                    // ── Delay para que el StateFlow actualice antes de navegar ──
                                                                    scope.launch {
                                                                        delay(300)
                                                                        onBack()
                                                                    }
                                                                }
                                                            }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("QrScanner", "Error al iniciar cámara", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Marco de guía
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                        .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                )

                // Instrucción
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(24.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Centra el código QR dentro del recuadro",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Se necesita permiso de cámara para escanear el QR",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                        Text("Conceder permiso")
                    }
                }
            }
        }
    }
}

