package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TailorCardDark
import com.example.ui.theme.TailorSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun CalibrateDialog(
    currentRatio: Double,
    onRatioChanged: (Double) -> Unit,
    onCalibrateByHeight: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var heightInput by remember { mutableStateOf("175") }
    var sliderValue by remember { mutableStateOf(currentRatio.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "معايرة مقياس الرؤية البصرية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = TailorSurfaceDark,
                    contentColor = GoldPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("تلقائي (بالطول)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("يدوي (شريط التمرير)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    Text(
                        text = "أدخل الطول الفعلي للزبون بالسنتيمتر. يحسب النظام الرياضي بدقة متناهية معامل تحويل البكسل إلى سنتيمتر وفقاً للمسافة الحقيقية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("طول الزبون (سم)") },
                        placeholder = { Text("مثال: 175") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("height_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المعامل الحالي: ${String.format("%.4f", currentRatio)} سم/بكسل",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "اضبط معامل القياس بدقة (+/- أو السحب). زيادة المعامل تزيد السنتيمترات المحسوبة للمقاسات.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = TailorCardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${String.format("%.3f", sliderValue)} سم/بكسل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = GoldPrimary
                            )

                            Slider(
                                value = sliderValue,
                                onValueChange = {
                                    sliderValue = it
                                    onRatioChanged(it.toDouble())
                                },
                                valueRange = 0.05f..0.40f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary
                                ),
                                modifier = Modifier.testTag("scale_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val next = (sliderValue - 0.005f).coerceAtLeast(0.01f)
                                        sliderValue = next
                                        onRatioChanged(next.toDouble())
                                    },
                                    modifier = Modifier.testTag("ratio_minus_btn")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = Color.White)
                                }

                                Text("الخطوة ±0.005", fontSize = 11.sp, color = TextMuted)

                                IconButton(
                                    onClick = {
                                        val next = (sliderValue + 0.005f).coerceAtMost(0.50f)
                                        sliderValue = next
                                        onRatioChanged(next.toDouble())
                                    },
                                    modifier = Modifier.testTag("ratio_plus_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "زيادة", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTab == 0) {
                        val parsed = heightInput.toDoubleOrNull() ?: 175.0
                        onCalibrateByHeight(parsed)
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("apply_calibration_btn")
            ) {
                Text(if (selectedTab == 0) "تطبيق المعايرة الآن" else "تم", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        containerColor = TailorSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}
