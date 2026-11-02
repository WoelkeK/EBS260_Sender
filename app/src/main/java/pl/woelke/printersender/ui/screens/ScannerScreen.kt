package pl.woelke.printersender.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import pl.woelke.printersender.viewmodel.MainViewModel


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission(onPermissionGranted: @Composable () -> Unit) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Jeśli uprawnienia zostały przyznane, uruchamiamy ekran
    if (cameraPermissionState.status.isGranted) {
        onPermissionGranted()
    } else {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Aplikacja wymaga dostępu do kamery, aby skanować kody kreskowe.",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(26.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Zezwalaj na dostęp do kamery")
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(navController: NavHostController, mainViewModel: MainViewModel) {
    RequestCameraPermission {

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val previewView = remember { PreviewView(context) }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)


        var lastScannedValue by remember { mutableStateOf<String?>(null) }
        var lastScanTime by remember { mutableStateOf(0L) }
        val minimumTimeBetweenScans = 4000L
        var isScanning by remember { mutableStateOf(false) }

        var borderColor by remember { mutableStateOf(Color.White) }
        var lastScannedFormat by remember { mutableStateOf<String?>(null) }
        var selectedType by remember { mutableStateOf(ALL_TYPES) }


        val barcodeScanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            ) { preview ->
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    val previewUseCase = Preview.Builder()
                        .build()
                        .apply { setSurfaceProvider(preview.surfaceProvider) }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage =
                                com.google.mlkit.vision.common.InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                            val screenWidth = preview.width.toFloat()
                            val screenHeight = preview.height.toFloat()

                            val rectWidth = screenWidth * 0.5f
                            val rectHeight = screenHeight * 0.3f

                            val left = (screenWidth - rectWidth) / 2f
                            val top = (screenHeight - rectHeight) / 2f - screenHeight * 0.22f
                            val right = left + rectWidth
                            val bottom = top + rectHeight

                            val scaleX = screenWidth / imageProxy.height.toFloat()
                            val scaleY = screenHeight / imageProxy.width.toFloat()

                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val boundingBox = barcode.boundingBox
                                        if (boundingBox != null) {

                                            val translatedLeft = boundingBox.left * scaleX
                                            val translatedTop = boundingBox.top * scaleY
                                            val translatedRight = boundingBox.right * scaleX
                                            val translatedBottom = boundingBox.bottom * scaleY

                                            if (translatedLeft >= left &&
                                                translatedTop >= top &&
                                                translatedRight <= right &&
                                                translatedBottom <= bottom
                                            ) {

                                                // Kod w ramce
                                                val rawValue = barcode.rawValue
                                                val currentTime = System.currentTimeMillis()
                                                val isTypeMatching =
                                                    selectedType == ALL_TYPES || barcode.format == selectedType

                                                if (!rawValue.isNullOrEmpty() && rawValue != lastScannedValue && currentTime - lastScanTime > minimumTimeBetweenScans && !isScanning && isTypeMatching
                                                ) {
                                                    // Zablokuj i zapamiętaj dane
                                                    isScanning = true
                                                    lastScannedValue = rawValue
                                                    lastScanTime = currentTime

                                                    lastScannedFormat = when (barcode.format) {
                                                        Barcode.FORMAT_QR_CODE -> "QR Code"
                                                        Barcode.FORMAT_CODE_128 -> "Code 128"
                                                        Barcode.FORMAT_CODE_39 -> "Code 39"
                                                        Barcode.FORMAT_EAN_13 -> "EAN-13"
                                                        Barcode.FORMAT_EAN_8 -> "EAN-8"
                                                        Barcode.FORMAT_UPC_A -> "UPC-A"
                                                        Barcode.FORMAT_UPC_E -> "UPC-E"
                                                        Barcode.FORMAT_ITF -> "ITF"
                                                        Barcode.FORMAT_PDF417 -> "PDF-417"
                                                        Barcode.FORMAT_AZTEC -> "Aztec"
                                                        else -> "Nieznany"
                                                    }

                                                    borderColor =
                                                        Color.Green

                                                    // Przetwarzanie
                                                    val processedData = processScannedData(rawValue)
                                                    mainViewModel.sendMessage(processedData)

                                                    kotlinx.coroutines.GlobalScope.launch {
                                                        kotlinx.coroutines.delay(
                                                            minimumTimeBetweenScans
                                                        )
                                                        borderColor = Color.White
                                                        lastScannedValue = null
                                                        lastScannedFormat = null
                                                        isScanning = false
                                                    }
                                                } else {
                                                    // Kod jest fragmentaryczny (ITF lub inne)
                                                    borderColor =
                                                        Color.Yellow
                                                }
                                            } else {
                                                borderColor = Color.Red
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e -> e.printStackTrace() }
                                .addOnCompleteListener { imageProxy.close() }

                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val rectWidth = size.width * 0.8f
                val rectHeight = size.height * 0.3f

                val left = (size.width - rectWidth) / 2f
                val top = (size.height - rectHeight) / 2f - size.height * 0.22f

                drawRect(
                    color = borderColor,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                )
            }

            // Wyświetlanie danych zeskanowanego kodu
            if (!lastScannedValue.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 220.dp),

                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    Text(
                        text = "Kod: $lastScannedFormat",
                        fontSize = 20.sp,
                        color = Color(255, 255, 255, 170),
                    )
                    Text(
                        text = lastScannedValue.orEmpty(),
                        fontSize = 14.sp,
                        color = Color(255, 255, 255, 155),
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center

                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)

                    .padding(top = 20.dp)
            ) {
                BarcodeTypeSelectorSelectField(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.8f)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 66.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        cameraProviderFuture.get().unbindAll()
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        text = "Zamknij",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(12.dp)

                    )
                }
            }
        }
    }
}

fun processScannedData(rawData: String?): String {
    val trimmedData = rawData?.trim().orEmpty()
    return if (trimmedData.length < 399) {
        trimmedData
    } else {
        "Niepoprawny kod"
    }
}
