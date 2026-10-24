package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
import com.example.ui.theme.TailorCardDark
import com.example.ui.theme.TailorDarkBg
import com.example.ui.theme.TailorSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TailorViewModel

@Composable
fun PhotoMeasureScreen(
    viewModel: TailorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoUri by viewModel.photoUri.collectAsState()
    val photoResult by viewModel.photoResult.collectAsState()
    val isProcessing by viewModel.isPhotoProcessing.collectAsState()
    val pixelToCmRatio by viewModel.pixelToCmRatio.collectAsState()
    val unit by viewModel.unit.collectAsState()
    val showCalibrateDialog by viewModel.showCalibrateDialog.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.setPhotoUri(context, uri)
    }

    val measurements = photoResult?.measurements ?: BodyMeasurements()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TailorDarkBg)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مسح وتحليل صورة القياس",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "اختر صورة كاملة للجسم لاستخراج أبعاد ومقاسات الخياطة بدقة",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("pick_photo_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("اختيار صورة", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Viewport or Placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(TailorSurfaceDark)
                .border(1.2.dp, TailorCardBorder, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "صورة قياس الزبون",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Pose skeleton overlay on photo
                PoseOverlay(
                    measurements = measurements,
                    isMirrored = false,
                    unit = unit,
                    modifier = Modifier.fillMaxSize()
                )

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري تحليل أبعاد ونقاط الجسم بالذكاء الاصطناعي...",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لم يتم اختيار صورة بعد",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "التقط أو اختر صورة واضحة لكامل الجسم يقف فيها الزبون مستقيماً باتجاه الكاميرا مع تباعد خفيف للذراعين.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("choose_photo_empty_btn")
                    ) {
                        Text("اختيار من معرض الصور")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Measurements & actions panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TailorCardDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المقاسات المستخرجة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "المعامل: ${String.format("%.3f", pixelToCmRatio)}",
                            fontSize = 12.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.setShowCalibrateDialog(true) },
                            modifier = Modifier.height(30.dp).testTag("photo_calibrate_btn")
                        ) {
                            Text("معايرة", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhotoMeasurementChip("الكتف", measurements.shoulderCm, NeonYellow, unit, Modifier.weight(1f))
                    PhotoMeasurementChip("الجذع", measurements.torsoCm, NeonCyan, unit, Modifier.weight(1f))
                    PhotoMeasurementChip("الكم", measurements.sleeveCm, NeonOrange, unit, Modifier.weight(1f))
                    PhotoMeasurementChip("الساق", measurements.legCm, NeonGreen, unit, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (photoUri != null) {
                        OutlinedButton(
                            onClick = { viewModel.reprocessPhoto(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("reprocess_photo_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة الفحص", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.setShowSaveDialog(true) },
                        enabled = measurements.isPoseDetected,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_photo_client_btn")
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ في سجل الزبائن", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showCalibrateDialog) {
        CalibrateDialog(
            currentRatio = pixelToCmRatio,
            onRatioChanged = {
                viewModel.setRatio(it)
                viewModel.reprocessPhoto(context)
            },
            onCalibrateByHeight = {
                viewModel.calibrateWithHeight(it)
                viewModel.reprocessPhoto(context)
            },
            onDismiss = { viewModel.setShowCalibrateDialog(false) }
        )
    }

    if (showSaveDialog) {
        SaveClientDialog(
            measurements = measurements,
            unit = unit,
            onConfirmSave = { name, phone, garment, notes ->
                viewModel.saveClient(name, phone, garment, notes, measurements)
            },
            onDismiss = { viewModel.setShowSaveDialog(false) }
        )
    }
}

@Composable
private fun PhotoMeasurementChip(
    label: String,
    cmValue: Double,
    color: Color,
    unit: String,
    modifier: Modifier = Modifier
) {
    val displayVal = if (unit == "in") {
        String.format("%.1f إنش", cmValue / 2.54)
    } else {
        String.format("%.1f سم", cmValue)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TailorSurfaceDark)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = displayVal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
