package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientEntity
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
import java.util.Locale

enum class ChartMetric(val arabicName: String, val color: Color) {
    WAIST("محيط الخصر", NeonOrange),
    CHEST("محيط الصدر", NeonCyan),
    SHOULDER("عرض الكتفين", NeonYellow),
    LEG("طول الساق", NeonGreen)
}

data class ChartPoint(
    val labelDate: String,
    val value: Double
)

@Composable
fun MeasurementEvolutionChart(
    client: ClientEntity,
    historyList: List<ClientEntity>,
    unit: String = "سم",
    onAddNewCheckin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.WAIST) }

    // Prepare chronological list of records for this client
    val records = remember(historyList, client) {
        if (historyList.isNotEmpty()) {
            historyList.sortedBy { it.id }
        } else {
            listOf(client)
        }
    }

    // Build data points based on selected metric
    val dataPoints: List<ChartPoint> = remember(records, selectedMetric) {
        records.map { record ->
            val v = when (selectedMetric) {
                ChartMetric.WAIST -> if (record.waist > 0) record.waist else record.torso * 1.6
                ChartMetric.CHEST -> if (record.chest > 0) record.chest else record.shoulder * 2.15
                ChartMetric.SHOULDER -> record.shoulder
                ChartMetric.LEG -> record.leg
            }
            // Extract short date (e.g. MM-DD) from timestamp
            val dateStr = record.timestamp.take(10).substringAfter("-")
            ChartPoint(dateStr, v)
        }
    }

    val latestValue = dataPoints.lastOrNull()?.value ?: 0.0
    val firstValue = dataPoints.firstOrNull()?.value ?: 0.0
    val diff = latestValue - firstValue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = TailorSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, TailorCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Arabic title and trend indicator
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
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مخطط تطور القياسات عبر الزمن",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "متابعة دقيقة لتغيرات مقاسات الزبون ومؤشرات التفصيل",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Add follow-up measurement button
                IconButton(
                    onClick = onAddNewCheckin,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .testTag("add_history_checkin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "تسجيل قياس جديد",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Selector Chips (Waist, Chest, Shoulder, Leg)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChartMetric.values().forEach { metric ->
                    val isSelected = selectedMetric == metric
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) metric.color else TailorCardDark)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) metric.color else TailorCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedMetric = metric }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("chart_metric_${metric.name.lowercase()}")
                    ) {
                        Text(
                            text = metric.arabicName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Banner: Latest value and Delta change
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TailorCardDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "آخر قياس مسجل", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${String.format(Locale.US, "%.1f", latestValue)} $unit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedMetric.color
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "عدد الجلسات", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${dataPoints.size} قياسات",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "معدل التغير", fontSize = 10.sp, color = TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when {
                            diff > 0.3 -> Icons.Default.TrendingUp
                            diff < -0.3 -> Icons.Default.TrendingDown
                            else -> Icons.Default.TrendingFlat
                        }
                        val diffColor = when {
                            diff > 0.3 -> NeonOrange
                            diff < -0.3 -> NeonGreen
                            else -> TextSecondary
                        }
                        Icon(icon, contentDescription = null, tint = diffColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        val sign = if (diff > 0) "+" else ""
                        Text(
                            text = "$sign${String.format(Locale.US, "%.1f", diff)} $unit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = diffColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D1424))
                    .border(1.dp, TailorCardBorder, RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp, horizontal = 14.dp)
            ) {
                EvolutionLineChart(
                    points = dataPoints,
                    lineColor = selectedMetric.color,
                    unit = unit
                )
            }
        }
    }
}

@Composable
private fun EvolutionLineChart(
    points: List<ChartPoint>,
    lineColor: Color,
    unit: String
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val leftPadding = 36f
        val rightPadding = 36f
        val topPadding = 30f
        val bottomPadding = 32f

        val plotWidth = w - leftPadding - rightPadding
        val plotHeight = h - topPadding - bottomPadding

        if (points.isEmpty()) return@Canvas

        // If only 1 record exists, create a baseline visual context so the user sees a clear trend graph
        val displayPoints = if (points.size == 1) {
            val p = points.first()
            listOf(
                ChartPoint("سابق", p.value - 1.0),
                ChartPoint(p.labelDate, p.value)
            )
        } else {
            points
        }

        val values = displayPoints.map { it.value }
        val minVal = (values.minOrNull() ?: 0.0) - 2.0
        val maxVal = (values.maxOrNull() ?: 100.0) + 2.0
        val valRange = (maxVal - minVal).coerceAtLeast(1.0)

        // Draw 3 horizontal guide lines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = topPadding + plotHeight * (i.toFloat() / gridLines)
            val gridVal = maxVal - (valRange * (i.toFloat() / gridLines))

            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(leftPadding, y),
                end = Offset(w - rightPadding, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Y-axis value text
            val textLayout = textMeasurer.measure(
                text = "${gridVal.toInt()}",
                style = TextStyle(fontSize = 9.sp, color = TextMuted)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(4f, y - textLayout.size.height / 2f)
            )
        }

        // Calculate (x, y) coordinates for each point
        val coords = displayPoints.mapIndexed { index, point ->
            val fractionX = if (displayPoints.size > 1) {
                index.toFloat() / (displayPoints.size - 1)
            } else 0.5f
            val x = leftPadding + plotWidth * fractionX
            val fractionY = ((point.value - minVal) / valRange).toFloat()
            val y = topPadding + plotHeight * (1f - fractionY)
            Offset(x, y)
        }

        // 1. Draw gradient fill under the line
        val fillPath = Path().apply {
            moveTo(coords.first().x, topPadding + plotHeight)
            lineTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val prev = coords[i - 1]
                val curr = coords[i]
                val cpx = (prev.x + curr.x) / 2f
                cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
            }
            lineTo(coords.last().x, topPadding + plotHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.03f)
                ),
                startY = topPadding,
                endY = topPadding + plotHeight
            )
        )

        // 2. Draw smooth trend line
        val strokePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val prev = coords[i - 1]
                val curr = coords[i]
                val cpx = (prev.x + curr.x) / 2f
                cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
            }
        }

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Draw glowing point indicators & labels
        coords.forEachIndexed { i, offset ->
            val pt = displayPoints[i]

            // Outer glow
            drawCircle(
                color = lineColor.copy(alpha = 0.35f),
                radius = 9f,
                center = offset
            )
            // Outer ring
            drawCircle(
                color = lineColor,
                radius = 5.5f,
                center = offset
            )
            // Center white dot
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = offset
            )

            // Value text above the point
            val valText = textMeasurer.measure(
                text = "${String.format(Locale.US, "%.1f", pt.value)}",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            drawText(
                textLayoutResult = valText,
                topLeft = Offset(offset.x - valText.size.width / 2f, offset.y - 20f)
            )

            // Date text below the chart
            val dateText = textMeasurer.measure(
                text = pt.labelDate,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            )
            drawText(
                textLayoutResult = dateText,
                topLeft = Offset(offset.x - dateText.size.width / 2f, topPadding + plotHeight + 8f)
            )
        }
    }
}
