package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.model.BodyMeasurements
import com.example.model.Point2D
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.google.mlkit.vision.pose.PoseLandmark

@Composable
fun PoseOverlay(
    measurements: BodyMeasurements,
    isMirrored: Boolean = false,
    unit: String = "cm",
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Posture alignment guidelines (A-pose guide frame)
        val guideLeft = w * 0.12f
        val guideRight = w * 0.88f
        val guideTop = h * 0.08f
        val guideBottom = h * 0.92f

        drawRoundRect(
            color = Color.White.copy(alpha = 0.18f),
            topLeft = Offset(guideLeft, guideTop),
            size = androidx.compose.ui.geometry.Size(guideRight - guideLeft, guideBottom - guideTop),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
            style = Stroke(
                width = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        )

        if (!measurements.isPoseDetected || measurements.landmarks.isEmpty()) {
            return@Canvas
        }

        fun getPoint(type: Int): Offset? {
            val pt = measurements.landmarks[type] ?: return null
            val px = if (isMirrored) (1f - pt.x) * w else pt.x * w
            val py = pt.y * h
            return Offset(px, py)
        }

        // Draw basic skeleton connections
        val connections = listOf(
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
            PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
            PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
            PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
            PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
            PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE
        )

        for ((startType, endType) in connections) {
            val start = getPoint(startType)
            val end = getPoint(endType)
            if (start != null && end != null) {
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = start,
                    end = end,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 1. Highlight Shoulder Width Line (Vivid Yellow)
        val ls = getPoint(PoseLandmark.LEFT_SHOULDER)
        val rs = getPoint(PoseLandmark.RIGHT_SHOULDER)
        if (ls != null && rs != null) {
            drawLine(
                color = NeonYellow,
                start = ls,
                end = rs,
                strokeWidth = 6.5f,
                cap = StrokeCap.Round
            )
            val unitLabel = if (unit == "cm") "سم" else "إنش"
            // Midpoint text for shoulder
            val midX = (ls.x + rs.x) / 2f
            val midY = (ls.y + rs.y) / 2f - 22f
            drawMeasurementBadge(
                textMeasurer = textMeasurer,
                text = "الكتف: ${formatValue(measurements.shoulderCm, unit)} $unitLabel",
                position = Offset(midX, midY),
                badgeColor = NeonYellow,
                textColor = Color.Black
            )
        }

        // 2. Highlight Torso Length (Neon Cyan Spine)
        val lh = getPoint(PoseLandmark.LEFT_HIP)
        val rh = getPoint(PoseLandmark.RIGHT_HIP)
        if (ls != null && rs != null && lh != null && rh != null) {
            val unitLabel = if (unit == "cm") "سم" else "إنش"
            val midShoulder = Offset((ls.x + rs.x) / 2f, (ls.y + rs.y) / 2f)
            val midHip = Offset((lh.x + rh.x) / 2f, (lh.y + rh.y) / 2f)
            drawLine(
                color = NeonCyan,
                start = midShoulder,
                end = midHip,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            val midTorso = Offset((midShoulder.x + midHip.x) / 2f + 25f, (midShoulder.y + midHip.y) / 2f)
            drawMeasurementBadge(
                textMeasurer = textMeasurer,
                text = "الجذع: ${formatValue(measurements.torsoCm, unit)} $unitLabel",
                position = midTorso,
                badgeColor = NeonCyan,
                textColor = Color.Black
            )
        }

        // 3. Highlight Sleeve Length (Neon Orange)
        val lw = getPoint(PoseLandmark.LEFT_WRIST)
        if (ls != null && lw != null) {
            val unitLabel = if (unit == "cm") "سم" else "إنش"
            drawLine(
                color = NeonOrange,
                start = ls,
                end = lw,
                strokeWidth = 5.5f,
                cap = StrokeCap.Round
            )
            val midSleeve = Offset((ls.x + lw.x) / 2f - 30f, (ls.y + lw.y) / 2f)
            drawMeasurementBadge(
                textMeasurer = textMeasurer,
                text = "الكم: ${formatValue(measurements.sleeveCm, unit)} $unitLabel",
                position = midSleeve,
                badgeColor = NeonOrange,
                textColor = Color.Black
            )
        }

        // 4. Highlight Leg Length (Neon Green)
        val la = getPoint(PoseLandmark.LEFT_ANKLE)
        if (lh != null && la != null) {
            val unitLabel = if (unit == "cm") "سم" else "إنش"
            drawLine(
                color = NeonGreen,
                start = lh,
                end = la,
                strokeWidth = 5.5f,
                cap = StrokeCap.Round
            )
            val midLeg = Offset((lh.x + la.x) / 2f - 30f, (lh.y + la.y) / 2f)
            drawMeasurementBadge(
                textMeasurer = textMeasurer,
                text = "الساق: ${formatValue(measurements.legCm, unit)} $unitLabel",
                position = midLeg,
                badgeColor = NeonGreen,
                textColor = Color.Black
            )
        }

        // Draw Landmark Points (Glowing dots)
        measurements.landmarks.forEach { (type, pt) ->
            val px = if (isMirrored) (1f - pt.x) * w else pt.x * w
            val py = pt.y * h
            val point = Offset(px, py)

            // Outer glow
            drawCircle(
                color = GoldPrimary.copy(alpha = 0.4f),
                radius = 11f,
                center = point
            )
            // Inner center
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = point
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMeasurementBadge(
    textMeasurer: TextMeasurer,
    text: String,
    position: Offset,
    badgeColor: Color,
    textColor: Color
) {
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    )

    val paddingX = 14f
    val paddingY = 8f
    val badgeWidth = textLayoutResult.size.width + paddingX * 2
    val badgeHeight = textLayoutResult.size.height + paddingY * 2
    val topLeft = Offset(position.x - badgeWidth / 2f, position.y - badgeHeight / 2f)

    drawRoundRect(
        color = badgeColor,
        topLeft = topLeft,
        size = androidx.compose.ui.geometry.Size(badgeWidth, badgeHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(topLeft.x + paddingX, topLeft.y + paddingY)
    )
}

private fun formatValue(cmValue: Double, unit: String): String {
    return if (unit == "in") {
        String.format("%.1f", cmValue / 2.54)
    } else {
        String.format("%.1f", cmValue)
    }
}
