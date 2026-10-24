package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientEntity
import com.example.model.BodyMeasurements
import com.example.ui.components.MeasurementEvolutionChart
import com.example.ui.theme.ErrorRed
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
import com.example.util.CsvExporter
import com.example.viewmodel.TailorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientDetailScreen(
    client: ClientEntity,
    viewModel: TailorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val historyList by viewModel.selectedClientHistory.collectAsState()

    var displayUnit by remember { mutableStateOf("cm") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showExportCsvDialog by remember { mutableStateOf(false) }
    var showNewCheckinDialog by remember { mutableStateOf(false) }
    var copiedToast by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val success = CsvExporter.writeCsvToUri(context, uri, listOf(client))
            if (success) {
                Toast.makeText(context, "تم تصدير ملف المقاسات بنجاح!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TailorDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(TailorSurfaceDark)
                    .testTag("detail_back_btn")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Unit Switch
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TailorSurfaceDark)
                        .border(1.dp, TailorCardBorder, RoundedCornerShape(8.dp))
                        .clickable { displayUnit = if (displayUnit == "cm") "in" else "cm" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (displayUnit == "cm") "الوحدة: سم" else "الوحدة: بوصة",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Export CSV Icon Button
                IconButton(
                    onClick = { showExportCsvDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(TailorSurfaceDark)
                        .testTag("detail_export_csv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "تصدير CSV",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(TailorSurfaceDark)
                        .testTag("detail_edit_btn")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(TailorSurfaceDark)
                        .testTag("detail_delete_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Client Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TailorCardDark),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, TailorCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = client.name.take(1).uppercase(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = client.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            if (client.phone.isNotBlank()) {
                                Text(
                                    text = client.phone,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = client.garmentType,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "تاريخ تسجيل القياس: ${client.timestamp}",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                if (client.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TailorSurfaceDark)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ملاحظات وتفاصيل التفصيل: ${client.notes}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Body Silhouette & Measurements Anatomy Section
        Text(
            text = "تفاصيل وتوزيع قياسات الجسم التشريحية",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TailorSurfaceDark),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Interactive Mannequin Canvas + Measurement Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mannequin Silhouette
                    Box(
                        modifier = Modifier
                            .size(110.dp, 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TailorCardDark)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MannequinCanvas()
                    }

                    // Key Measurement Detail List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("عرض الكتفين", client.shoulder, displayUnit, NeonYellow)
                        DetailRow("طول الجذع", client.torso, displayUnit, NeonCyan)
                        DetailRow("طول الكم", client.sleeve, displayUnit, NeonOrange)
                        DetailRow("طول الساق", client.leg, displayUnit, NeonGreen)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Derived Tailor Measurements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chestVal = if (client.chest > 0) client.chest else client.shoulder * 2.15
                    val waistVal = if (client.waist > 0) client.waist else client.torso * 1.6
                    val heightVal = if (client.height > 0) client.height else client.torso + client.leg + 35.0

                    DerivedTile("محيط الصدر", chestVal, displayUnit, Modifier.weight(1f))
                    DerivedTile("محيط الخصر", waistVal, displayUnit, Modifier.weight(1f))
                    DerivedTile("الطول الكامل", heightVal, displayUnit, Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Graphical Measurement Evolution Chart (باستخدام مكتبة الرسوم البيانية)
        MeasurementEvolutionChart(
            client = client,
            historyList = historyList,
            unit = if (displayUnit == "cm") "سم" else "بوصة",
            onAddNewCheckin = { showNewCheckinDialog = true }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Fabric Requirement Estimator Card
        FabricEstimatorCard(client = client)

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons: Share Slip, Copy, & Export CSV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val slipText = generateTailorSlip(client, displayUnit)
                    clipboardManager.setText(AnnotatedString(slipText))
                    copiedToast = true
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("copy_slip_btn")
            ) {
                Text(if (copiedToast) "تم النسخ!" else "نسخ السند", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showExportCsvDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("export_client_csv_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصدير CSV", fontSize = 12.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val slipText = generateTailorSlip(client, displayUnit)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "سند تفصيل - ${client.name}")
                        putExtra(Intent.EXTRA_TEXT, slipText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة سند المقاسات"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("share_slip_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مشاركة السند", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Export Single Client CSV Dialog
    if (showExportCsvDialog) {
        AlertDialog(
            onDismissRequest = { showExportCsvDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تصدير مقاسات الزبون (CSV)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "تصدير كامل القياسات التشريحية للزبون ${client.name} إلى ملف جدول بيانات قياسي متوافق مع Excel وبرامج الطباعة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "يتضمن الملف: الكتفين، الجذع، الكم، الساق، محيط الصدر، محيط الخصر، الطول الكامل، ونوع الثوب.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CsvExporter.shareClientsCsv(
                            context = context,
                            clients = listOf(client),
                            title = "مشاركة ملف مقاسات ${client.name}"
                        )
                        showExportCsvDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.testTag("share_single_csv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة الملف", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExportCsvDialog = false
                        val cleanName = client.name.replace("\\s+".toRegex(), "_").take(15)
                        createDocumentLauncher.launch("${cleanName}_measurements.csv")
                    },
                    modifier = Modifier.testTag("save_single_csv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ بالجهاز")
                }
            },
            containerColor = TailorSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف سجل الزبون") },
            text = { Text("هل أنت متأكد من رغبتك في حذف سجل ومقاسات الزبون ${client.name} نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(client)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            },
            containerColor = TailorSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit Measurements Dialog
    if (showEditDialog) {
        EditClientDialog(
            client = client,
            onSave = { updated ->
                viewModel.updateClient(updated)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    // New Follow-Up Check-in Dialog
    if (showNewCheckinDialog) {
        NewFollowUpDialog(
            client = client,
            onSaveNewVisit = { newVisit ->
                viewModel.saveClient(
                    name = newVisit.name,
                    phone = newVisit.phone,
                    garmentType = newVisit.garmentType,
                    notes = newVisit.notes,
                    customMeasurements = BodyMeasurements(
                        shoulderCm = newVisit.shoulder,
                        torsoCm = newVisit.torso,
                        sleeveCm = newVisit.sleeve,
                        legCm = newVisit.leg,
                        chestCm = newVisit.chest,
                        waistCm = newVisit.waist,
                        heightCm = newVisit.height
                    )
                )
                showNewCheckinDialog = false
            },
            onDismiss = { showNewCheckinDialog = false }
        )
    }
}

@Composable
private fun MannequinCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val headCenter = Offset(w * 0.5f, h * 0.16f)
        val headRadius = h * 0.08f

        // Head
        drawCircle(color = Color.White.copy(alpha = 0.3f), radius = headRadius, center = headCenter)

        // Shoulders
        val leftShoulder = Offset(w * 0.22f, h * 0.28f)
        val rightShoulder = Offset(w * 0.78f, h * 0.28f)
        drawLine(color = NeonYellow, start = leftShoulder, end = rightShoulder, strokeWidth = 4f, cap = StrokeCap.Round)

        // Spine / Torso
        val midShoulder = Offset(w * 0.5f, h * 0.28f)
        val midHip = Offset(w * 0.5f, h * 0.58f)
        drawLine(color = NeonCyan, start = midShoulder, end = midHip, strokeWidth = 4.5f, cap = StrokeCap.Round)

        // Arms / Sleeves
        val leftWrist = Offset(w * 0.12f, h * 0.56f)
        val rightWrist = Offset(w * 0.88f, h * 0.56f)
        drawLine(color = NeonOrange, start = leftShoulder, end = leftWrist, strokeWidth = 3.5f, cap = StrokeCap.Round)
        drawLine(color = NeonOrange, start = rightShoulder, end = rightWrist, strokeWidth = 3.5f, cap = StrokeCap.Round)

        // Hips
        val leftHip = Offset(w * 0.35f, h * 0.58f)
        val rightHip = Offset(w * 0.65f, h * 0.58f)
        drawLine(color = Color.White.copy(alpha = 0.4f), start = leftHip, end = rightHip, strokeWidth = 3f)

        // Legs
        val leftAnkle = Offset(w * 0.32f, h * 0.94f)
        val rightAnkle = Offset(w * 0.68f, h * 0.94f)
        drawLine(color = NeonGreen, start = leftHip, end = leftAnkle, strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(color = NeonGreen, start = rightHip, end = rightAnkle, strokeWidth = 4f, cap = StrokeCap.Round)
    }
}

@Composable
private fun DetailRow(label: String, cmValue: Double, unit: String, color: Color) {
    val displayVal = if (unit == "in") {
        String.format("%.1f بوصة", cmValue / 2.54)
    } else {
        String.format("%.1f سم", cmValue)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
        }
        Text(text = displayVal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun DerivedTile(label: String, cmValue: Double, unit: String, modifier: Modifier = Modifier) {
    val displayVal = if (unit == "in") {
        String.format("%.1f بوصة", cmValue / 2.54)
    } else {
        String.format("%.1f سم", cmValue)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TailorCardDark)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = displayVal, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
        }
    }
}

@Composable
private fun FabricEstimatorCard(client: ClientEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TailorCardDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حاسبة أمتار الأقمشة للتفصيل",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val thobeMeters = String.format("%.2f", (client.torso + client.leg + client.sleeve + 20.0) * 2.0 / 100.0)
            val suitMeters = String.format("%.2f", (client.torso + client.sleeve + 25.0 + client.leg + 15.0) / 100.0 * 1.5)
            val shirtMeters = String.format("%.2f", (client.torso + client.sleeve + 15.0) / 100.0 * 1.35)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FabricEstimateRow("ثوب / كندورة (طاقة عرضين)", "$thobeMeters متر قماش")
                FabricEstimateRow("بدلة كاملة (جاكيت + بنطال)", "$suitMeters متر صوف/كتان")
                FabricEstimateRow("قميص رسمي مع البنطال", "$shirtMeters متر (عرض 140 سم)")
            }
        }
    }
}

@Composable
private fun FabricEstimateRow(item: String, estimate: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item, fontSize = 12.sp, color = TextSecondary)
        Text(text = estimate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
    }
}

@Composable
private fun EditClientDialog(
    client: ClientEntity,
    onSave: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(client.name) }
    var phone by remember { mutableStateOf(client.phone) }
    var shoulder by remember { mutableStateOf(client.shoulder.toString()) }
    var torso by remember { mutableStateOf(client.torso.toString()) }
    var sleeve by remember { mutableStateOf(client.sleeve.toString()) }
    var leg by remember { mutableStateOf(client.leg.toString()) }
    var waist by remember { mutableStateOf(if (client.waist > 0) client.waist.toString() else String.format(Locale.US, "%.1f", client.torso * 1.6)) }
    var notes by remember { mutableStateOf(client.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل مقاسات الزبون", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الزبون") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shoulder,
                        onValueChange = { shoulder = it },
                        label = { Text("الكتف (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = torso,
                        onValueChange = { torso = it },
                        label = { Text("الجذع (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sleeve,
                        onValueChange = { sleeve = it },
                        label = { Text("الكم (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = leg,
                        onValueChange = { leg = it },
                        label = { Text("الساق (سم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    label = { Text("محيط الخصر (سم)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات التفصيل") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = client.copy(
                        name = name.ifBlank { client.name },
                        phone = phone,
                        shoulder = shoulder.toDoubleOrNull() ?: client.shoulder,
                        torso = torso.toDoubleOrNull() ?: client.torso,
                        sleeve = sleeve.toDoubleOrNull() ?: client.sleeve,
                        leg = leg.toDoubleOrNull() ?: client.leg,
                        waist = waist.toDoubleOrNull() ?: client.waist,
                        notes = notes
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("حفظ التعديلات", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        },
        containerColor = TailorSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun NewFollowUpDialog(
    client: ClientEntity,
    onSaveNewVisit: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var waist by remember { mutableStateOf(if (client.waist > 0) client.waist.toString() else "84.0") }
    var chest by remember { mutableStateOf(if (client.chest > 0) client.chest.toString() else "98.0") }
    var shoulder by remember { mutableStateOf(client.shoulder.toString()) }
    var notes by remember { mutableStateOf("متابعة مقاسات بعد شهر") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل جلسة قياس جديدة للزبون", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "أدخل القياسات المحدثة لإضافتها مباشرة إلى مخطط التطور البياني للزبون ${client.name}:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = waist,
                    onValueChange = { waist = it },
                    label = { Text("محيط الخصر الجديد (سم)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chest,
                    onValueChange = { chest = it },
                    label = { Text("محيط الصدر الجديد (سم)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = shoulder,
                    onValueChange = { shoulder = it },
                    label = { Text("عرض الكتفين (سم)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الجلسة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val newVisit = client.copy(
                        id = 0,
                        timestamp = sdf.format(Date()),
                        waist = waist.toDoubleOrNull() ?: client.waist,
                        chest = chest.toDoubleOrNull() ?: client.chest,
                        shoulder = shoulder.toDoubleOrNull() ?: client.shoulder,
                        notes = notes
                    )
                    onSaveNewVisit(newVisit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("تسجيل وتحديث المخطط", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
        },
        containerColor = TailorSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}

private fun generateTailorSlip(client: ClientEntity, unit: String): String {
    val factor = if (unit == "in") 1.0 / 2.54 else 1.0
    val u = if (unit == "in") "بوصة" else "سم"
    return """
        ✂️ الذوق الرفيع لمقاسات الخياطة - سند تفصيل معتمد
        ═════════════════════════════════════════
        اسم الزبون: ${client.name}
        رقم الهاتف: ${client.phone}
        نوع الملبوس: ${client.garmentType}
        تاريخ أخذ القياس: ${client.timestamp}
        ─────────────────────────────────────────
        • عرض الكتفين:  ${String.format("%.1f", client.shoulder * factor)} $u
        • طول الجذع:    ${String.format("%.1f", client.torso * factor)} $u
        • طول الكم:     ${String.format("%.1f", client.sleeve * factor)} $u
        • طول الساق:    ${String.format("%.1f", client.leg * factor)} $u
        • محيط الصدر:   ${String.format("%.1f", (if (client.chest > 0) client.chest else client.shoulder * 2.15) * factor)} $u
        • محيط الخصر:   ${String.format("%.1f", (if (client.waist > 0) client.waist else client.torso * 1.6) * factor)} $u
        • الطول الكلي:   ${String.format("%.1f", (if (client.height > 0) client.height else client.torso + client.leg + 35.0) * factor)} $u
        ─────────────────────────────────────────
        ${if (client.notes.isNotBlank()) "ملاحظات وتفاصيل القماش: ${client.notes}\n" else ""}
        تم الاستخراج بدقة عبر نظام الذوق الرفيع الذكي للقياسات
    """.trimIndent()
}
