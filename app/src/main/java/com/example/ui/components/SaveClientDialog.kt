package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BodyMeasurements
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TailorCardDark
import com.example.ui.theme.TailorSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun SaveClientDialog(
    measurements: BodyMeasurements,
    unit: String = "cm",
    onConfirmSave: (name: String, phone: String, garment: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedGarment by remember { mutableStateOf("ثوب / دشداشة") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val garmentOptions = listOf(
        "ثوب / دشداشة",
        "بدلة رسمية",
        "قميص وبنطال",
        "جاكيت / بليزر",
        "عباية / بشت",
        "تفصيل مخصص"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "حفظ مقاسات الزبون",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Measurements summary chips
                Card(
                    colors = CardDefaults.cardColors(containerColor = TailorCardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "المقاسات الملتقطة (${if (unit == "cm") "سم" else "إنش"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MeasurementChip("الكتف", measurements.shoulderCm, NeonYellow, unit)
                            MeasurementChip("الجذع", measurements.torsoCm, NeonCyan, unit)
                            MeasurementChip("الكم", measurements.sleeveCm, NeonOrange, unit)
                            MeasurementChip("الساق", measurements.legCm, NeonGreen, unit)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("اسم الزبون *") },
                    placeholder = { Text("مثال: محمد الهاجري") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("يرجى إدخال اسم الزبون", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف (اختياري)") },
                    placeholder = { Text("مثال: 0501234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "نوع الزي المُراد تفصيله", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    garmentOptions.forEach { garment ->
                        val isSelected = selectedGarment == garment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else TailorCardDark)
                                .clickable { selectedGarment = garment }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = garment,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الخياطة (القماش، الياقة، الكبك)") },
                    placeholder = { Text("مثال: قماش كتان ياباني، ياقة سادة، كبك فرنسي") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirmSave(name.trim(), phone.trim(), selectedGarment, notes.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("save_client_confirm_btn")
            ) {
                Text("حفظ في السجل", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        containerColor = TailorSurfaceDark,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun MeasurementChip(label: String, cmValue: Double, color: Color, unit: String) {
    val displayVal = if (unit == "in") {
        String.format("%.1f إنش", cmValue / 2.54)
    } else {
        String.format("%.1f سم", cmValue)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Text(text = label, fontSize = 10.sp, color = color)
            Text(text = displayVal, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
