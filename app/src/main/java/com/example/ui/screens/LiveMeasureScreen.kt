package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.PoseAnalyzer
import com.example.model.BodyMeasurements
import com.example.ui.components.CalibrateDialog
import com.example.ui.components.PoseOverlay
import com.example.ui.components.SaveClientDialog
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TailorCardBorder
import com.example.ui.theme.TailorDarkBg
import com.example.ui.theme.TailorSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TailorViewModel
import java.util.concurrent.Executors

@Composable
fun LiveMeasureScreen(
    viewModel: TailorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val liveMeasurements by viewModel.liveMeasurements.collectAsState()
    val frozenMeasurements by viewModel.frozenMeasurements.collectAsState()
    val isFrozen by viewModel.isFrozen.collectAsState()
    val pixelToCmRatio by viewModel.pixelToCmRatio.collectAsState()
    val unit by viewModel.unit.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()
    val showCalibrateDialog by viewModel.showCalibrateDialog.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val activeMeasurements = if (isFrozen) (frozenMeasurements ?: liveMeasurements) else liveMeasurements

    Box(modifier = modifier.fillMaxSize().background(TailorDarkBg)) {
        if (hasCameraPermission) {
            // Camera Preview + Analyzer
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val poseAnalyzer = PoseAnalyzer(pixelToCmRatio = pixelToCmRatio) { measurements, heightPx ->
                            viewModel.updateLiveMeasurements(measurements, heightPx)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, poseAnalyzer)
                            }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Canvas skeleton & measurement dimension lines
            PoseOverlay(
                measurements = activeMeasurements,
                isMirrored = lensFacing == CameraSelector.LENS_FACING_FRONT,
                unit = unit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission fallback card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "إذن الوصول إلى الكاميرا مطلوب",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "يستخدم نظام الذوق الرفيع كاميرا الجهاز لتتبع أبعاد الجسم وقياسات الخياطة بدقة فورية فائقة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.testTag("grant_camera_permission_btn")
                ) {
                    Text("منح إذن تشغيل الكاميرا", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Control Bar: Lens flip & Unit switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, TailorCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activeMeasurements.isPoseDetected) NeonGreen else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeMeasurements.isPoseDetected) "تم تثبيت وضعية الجسم" else "وجّه الزبون داخل الإطار",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Unit Switch Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, TailorCardBorder, RoundedCornerShape(10.dp))
                ) {
                    IconButton(
                        onClick = { viewModel.toggleUnit() },
                        modifier = Modifier.testTag("toggle_unit_btn")
                    ) {
                        Text(
                            text = if (unit == "cm") "سم" else "إنش",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Lens switch
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, TailorCardBorder, RoundedCornerShape(10.dp))
                ) {
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.testTag("switch_camera_btn")
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "تبديل الكاميرا", tint = Color.White)
                    }
                }
            }
        }

        // Bottom Dashboard Card (Matching the Python HUD Dashboard Layout)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            colors = CardDefaults.cardColors(containerColor = Color(0xF0101827)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, TailorCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header of HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الذوق الرفيع لمقاسات الخياطة - قياس حي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "الوقوف مستقيماً بوضعية A (تباعد الذراعين قليلاً)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    // Ratio scale indicator & quick adjust
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.adjustRatio(-0.005) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .testTag("hud_minus_btn")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تقليل المعامل", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = " ${String.format("%.3f", pixelToCmRatio)} ",
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        IconButton(
                            onClick = { viewModel.adjustRatio(0.005) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .testTag("hud_plus_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة المعامل", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The 4 Core Measurements from the Python script: Shoulder, Torso, Sleeve, Leg
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeasurementTile(
                        label = "الكتف",
                        cmValue = activeMeasurements.shoulderCm,
                        unit = unit,
                        color = NeonYellow,
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementTile(
                        label = "الجذع",
                        cmValue = activeMeasurements.torsoCm,
                        unit = unit,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementTile(
                        label = "الكم",
                        cmValue = activeMeasurements.sleeveCm,
                        unit = unit,
                        color = NeonOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MeasurementTile(
                        label = "الساق",
                        cmValue = activeMeasurements.legCm,
                        unit = unit,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action buttons: Freeze/Capture, Calibrate, Save Client
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Freeze / Resume Button
                    Button(
                        onClick = { viewModel.toggleFreeze() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFrozen) NeonCyan else Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("freeze_capture_btn")
                    ) {
                        Icon(
                            imageVector = if (isFrozen) Icons.Default.PlayArrow else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isFrozen) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFrozen) "استئناف" else "تجميد",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFrozen) Color.Black else Color.White
                        )
                    }

                    // Calibrate Button
                    Button(
                        onClick = { viewModel.setShowCalibrateDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_calibrate_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("معايرة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Save Client Button (Key 's' in Python script)
                    Button(
                        onClick = { viewModel.setShowSaveDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("open_save_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ ('s')", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }

    // Calibrate Dialog
    if (showCalibrateDialog) {
        CalibrateDialog(
            currentRatio = pixelToCmRatio,
            onRatioChanged = { viewModel.setRatio(it) },
            onCalibrateByHeight = { viewModel.calibrateWithHeight(it) },
            onDismiss = { viewModel.setShowCalibrateDialog(false) }
        )
    }

    // Save Client Dialog
    if (showSaveDialog) {
        SaveClientDialog(
            measurements = activeMeasurements,
            unit = unit,
            onConfirmSave = { name, phone, garment, notes ->
                viewModel.saveClient(name, phone, garment, notes, activeMeasurements)
            },
            onDismiss = { viewModel.setShowSaveDialog(false) }
        )
    }
}

@Composable
private fun MeasurementTile(
    label: String,
    cmValue: Double,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val displayVal = if (unit == "in") {
        String.format("%.1f إنش", cmValue / 2.54)
    } else {
        String.format("%.1f سم", cmValue)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = displayVal,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
