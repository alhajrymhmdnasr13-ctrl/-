package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.util.CsvExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientEntity
import com.example.model.BodyMeasurements
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
fun ClientsScreen(
    viewModel: TailorViewModel,
    onClientSelected: (ClientEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.clientsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val success = CsvExporter.writeCsvToUri(context, uri, clients)
            if (success) {
                Toast.makeText(context, "تم تصدير ملف المقاسات CSV بنجاح!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TailorDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سجل مقاسات الزبائن",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "الذوق الرفيع لمقاسات الخياطة (${clients.size} عميل)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export CSV Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TailorSurfaceDark)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable {
                                if (clients.isEmpty()) {
                                    Toast.makeText(context, "لا توجد سجلات زبائن للتصدير", Toast.LENGTH_SHORT).show()
                                } else {
                                    showExportDialog = true
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("export_csv_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تصدير CSV",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${clients.size} مسجل",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Top Search Bar
            val garmentFilter by viewModel.garmentFilter.collectAsState()
            val garmentFilterOptions = listOf("الكل", "ثوب / دشداشة", "بدلة رسمية", "قميص وبنطال", "عباية / بشت")

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "ابحث بالاسم أو رقم الهاتف أو الملاحظات...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = if (searchQuery.isNotEmpty()) GoldPrimary else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح البحث",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TailorSurfaceDark,
                    unfocusedContainerColor = TailorSurfaceDark,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = TailorCardBorder,
                    cursorColor = GoldPrimary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_clients_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                garmentFilterOptions.forEach { filterOption ->
                    val isSelected = (garmentFilter == filterOption) || (filterOption == "الكل" && garmentFilter == "All")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) GoldPrimary else TailorSurfaceDark)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldPrimary else TailorCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setGarmentFilter(if (filterOption == "الكل") "All" else filterOption) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_chip_${filterOption.take(5).lowercase().trim()}")
                    ) {
                        Text(
                            text = filterOption,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                    }
                }
            }

            // Results count & active search summary
            if (searchQuery.isNotBlank() || (garmentFilter != "All" && garmentFilter != "الكل")) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تم العثور على ${clients.size} سجل مطابق",
                        fontSize = 12.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "إعادة تعيين التصفية",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .clickable {
                                viewModel.setSearchQuery("")
                                viewModel.setGarmentFilter("All")
                            }
                            .testTag("reset_search_filter_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (clients.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TailorSurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotBlank() || (garmentFilter != "All" && garmentFilter != "الكل")) Icons.Default.Search else Icons.Default.People,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isBlank() && (garmentFilter == "All" || garmentFilter == "الكل"))
                                "لا توجد سجلات زبائن مسجلة"
                            else
                                "لم يتم العثور على نتائج مطابقة",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isBlank() && (garmentFilter == "All" || garmentFilter == "الكل"))
                                "قم بقياس الزبائن عبر الكاميرا الحية أو مسح الصورة، ثم اضغط حفظ لتسجيل قياسات الخياطة بدقة متناهية."
                            else
                                "لم يتم العثور على عملاء يطابقون \"$searchQuery\". تحقق من الاسم أو أعد ضبط التصفية.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        if (searchQuery.isNotBlank() || (garmentFilter != "All" && garmentFilter != "الكل")) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setGarmentFilter("All")
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_state_reset_btn")
                            ) {
                                Text("مسح البحث والتصفية")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(clients, key = { it.id }) { client ->
                        ClientCardItem(
                            client = client,
                            onClick = { onClientSelected(client) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }

        // FAB to add manual client
        FloatingActionButton(
            onClick = { showManualAddDialog = true },
            containerColor = GoldPrimary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_client_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مقاسات زبون")
        }
    }

    if (showManualAddDialog) {
        SaveClientDialog(
            measurements = BodyMeasurements(
                shoulderCm = 46.0,
                torsoCm = 52.0,
                sleeveCm = 62.0,
                legCm = 102.0
            ),
            unit = "cm",
            onConfirmSave = { name, phone, garment, notes ->
                viewModel.saveClient(
                    name = name,
                    phone = phone,
                    garmentType = garment,
                    notes = notes,
                    customMeasurements = BodyMeasurements(
                        shoulderCm = 46.0,
                        torsoCm = 52.0,
                        sleeveCm = 62.0,
                        legCm = 102.0
                    )
                )
                showManualAddDialog = false
            },
            onDismiss = { showManualAddDialog = false }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تصدير سجلات المقاسات (CSV)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "تصدير كافة سجلات الزبائن (${clients.size} سجل) بصيغة ملف CSV القياسية لفتحها في برامج Excel وجداول Google أو إرسالها للطباعة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TailorCardDark)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "أعمدة المقاسات المشمولة في التصدير:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "الاسم، رقم الهاتف، التاريخ، عرض الكتف، طول الجذع، طول الكم، طول الساق، الطول الكلي، محيط الصدر، محيط الخصر، نوع الزي، الملاحظات",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CsvExporter.shareClientsCsv(context, clients)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.testTag("share_csv_file_btn")
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
                        showExportDialog = false
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                        createDocumentLauncher.launch("tailor_clients_${sdf.format(Date())}.csv")
                    },
                    modifier = Modifier.testTag("save_device_csv_btn")
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
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun ClientCardItem(
    client: ClientEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("client_card_${client.id}"),
        colors = CardDefaults.cardColors(containerColor = TailorCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, TailorCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = client.name.take(1).uppercase(),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = client.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        if (client.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = client.phone, fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TailorSurfaceDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = client.garmentType,
                            fontSize = 11.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "عرض التفاصيل",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Measurement Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricMiniPill("الكتف", client.shoulder, NeonYellow, if (client.unit == "cm") "سم" else "إنش", Modifier.weight(1f))
                MetricMiniPill("الجذع", client.torso, NeonCyan, if (client.unit == "cm") "سم" else "إنش", Modifier.weight(1f))
                MetricMiniPill("الكم", client.sleeve, NeonOrange, if (client.unit == "cm") "سم" else "إنش", Modifier.weight(1f))
                MetricMiniPill("الساق", client.leg, NeonGreen, if (client.unit == "cm") "سم" else "إنش", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "التاريخ: ${client.timestamp}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                if (client.notes.isNotBlank()) {
                    Text(
                        text = "ملاحظات: ${client.notes.take(24)}…",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMiniPill(
    label: String,
    value: Double,
    color: Color,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 9.sp, color = color)
            Text(
                text = "${String.format("%.1f", value)} $unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
