package com.example.eventticketapp.ui.tickets.scanner

import android.Manifest
import android.media.MediaPlayer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.eventticketapp.R
import com.example.eventticketapp.data.model.Resource
import com.example.eventticketapp.data.model.Ticket
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    eventId: String,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val scanState by viewModel.scanState.collectAsState()
    val event by viewModel.event.collectAsState()
    val scanStats by viewModel.scanStats.collectAsState()

    // Permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val hasCameraPermission = cameraPermissionState.status.isGranted

    // Scanner state
    var flashEnabled by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var scannedTicket by remember { mutableStateOf<Ticket?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Camera control reference to toggle flash
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    // Animation for the scanner line
    val transition = rememberInfiniteTransition(label = "scanner")
    val scannerLinePosition by transition.animateFloat(
        initialValue = -100f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_line"
    )

    // Sound effects for scan result
    val successSound = remember { MediaPlayer.create(context, R.raw.success_sound) }
    val errorSound = remember { MediaPlayer.create(context, R.raw.error_sound) }

    // Clean up media players when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            successSound.release()
            errorSound.release()
        }
    }

    LaunchedEffect(key1 = eventId) {
        viewModel.loadEvent(eventId)
    }

    // Toggle Flash effect
    LaunchedEffect(flashEnabled, cameraControl) {
        cameraControl?.enableTorch(flashEnabled)
    }

    // Handle scan state changes
    LaunchedEffect(scanState) {
        when (scanState) {
            is Resource.Success -> {
                val ticket = (scanState as Resource.Success<Ticket>).data
                if (ticket != null) {
                    scannedTicket = ticket

                    // Sound and haptic feedback
                    successSound.start()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    showSuccessDialog = true
                }
                viewModel.clearScanState()
                isProcessing = false
            }
            is Resource.Error -> {
                errorMessage = (scanState as Resource.Error).message ?: "Unknown error"

                // Sound and haptic feedback
                errorSound.start()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                showErrorDialog = true
                viewModel.clearScanState()
                isProcessing = false
            }
            is Resource.Loading -> {
                // Loading state is handled by isProcessing
            }
            else -> {}
        }
    }

    // Success dialog
    if (showSuccessDialog && scannedTicket != null) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                scannedTicket = null
            },
            title = { Text("Success", color = MaterialTheme.colorScheme.primary) },
            icon = { Icon(Icons.Default.Check, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary) },
            text = {
                Column {
                    Text("Ticket successfully validated!")

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Ticket ID:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                scannedTicket!!.id,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "User ID:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                scannedTicket!!.userId,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        scannedTicket = null
                    }
                ) {
                    Text("Continue Scanning")
                }
            }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
            },
            title = { Text("Error", color = MaterialTheme.colorScheme.error) },
            icon = { Icon(Icons.Default.Close, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Tickets") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Flash toggle
                    if (hasCameraPermission) {
                        IconButton(onClick = { flashEnabled = !flashEnabled }) {
                            Icon(
                                if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = if (flashEnabled) "Disable Flash" else "Enable Flash"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Event info card
            event?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        scanStats?.let { stats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Scanned: ${stats.scannedCount}/${stats.totalCount}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "${(stats.scannedCount * 100 / (stats.totalCount.takeIf { it > 0 } ?: 1))}%",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = stats.scannedCount.toFloat() / (stats.totalCount.takeIf { it > 0 } ?: 1).toFloat(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Camera Preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (hasCameraPermission) {
                    CameraPreview(
                        onCameraControlReady = { control ->
                            cameraControl = control
                        },
                        onQrCodeScanned = { data ->
                            if (!isProcessing && !showSuccessDialog && !showErrorDialog) {
                                isProcessing = true
                                viewModel.processQrCode(data)
                            }
                        }
                    )

                    // Scanner overlay and animation
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Scanner target box
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )

                        // Animated scanner line
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth(0.7f)
                                .offset(y = Offset(0f, scannerLinePosition).y.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Scanning indicator
                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Instruction text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Position the QR code within the frame to scan",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    // No Camera Permission UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Camera permission is required to scan QR codes",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (!cameraPermissionState.status.isGranted) {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onCameraControlReady: (androidx.camera.core.CameraControl) -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // Image Analysis
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    processImageProxy(imageProxy, onQrCodeScanned)
                }
                
                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()
                    
                    // Bind use cases to camera
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    
                    // Pass camera control back
                    onCameraControlReady(camera.cameraControl)
                    
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
                
            }, executor)
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onQrCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        // Use ML Kit Barcode Scanner
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
            
        val scanner = BarcodeScanning.getClient(options)
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        onQrCodeScanned(value)
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
            .addOnCompleteListener {
                // Must close image proxy
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}