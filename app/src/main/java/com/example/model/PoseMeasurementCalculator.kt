package com.example.model

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.hypot

object PoseMeasurementCalculator {

    data class CalculationResult(
        val measurements: BodyMeasurements,
        val totalHeightPx: Float
    )

    fun calculate(
        pose: Pose,
        imageWidth: Float,
        imageHeight: Float,
        pixelToCmRatio: Double,
        smoother: MeasurementSmoother
    ): CalculationResult? {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

        if (leftShoulder == null || rightShoulder == null || leftHip == null || rightHip == null) {
            return null
        }

        // Store normalized landmarks for rendering
        val landmarksMap = mutableMapOf<Int, Point2D>()
        for (landmark in pose.allPoseLandmarks) {
            val normX = (landmark.position.x / imageWidth).coerceIn(0f, 1f)
            val normY = (landmark.position.y / imageHeight).coerceIn(0f, 1f)
            landmarksMap[landmark.landmarkType] = Point2D(normX, normY)
        }

        // 1. Shoulder width: distance between left and right shoulders
        val shoulderPx = hypot(
            rightShoulder.position.x - leftShoulder.position.x,
            rightShoulder.position.y - leftShoulder.position.y
        )
        val rawShoulder = shoulderPx * pixelToCmRatio

        // 2. Torso length: distance from mid-shoulder to mid-hip
        val midShoulderX = (leftShoulder.position.x + rightShoulder.position.x) / 2f
        val midShoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2f
        val midHipX = (leftHip.position.x + rightHip.position.x) / 2f
        val midHipY = (leftHip.position.y + rightHip.position.y) / 2f
        val torsoPx = hypot(midHipX - midShoulderX, midHipY - midShoulderY)
        val rawTorso = torsoPx * pixelToCmRatio

        // 3. Sleeve length: left or right shoulder to wrist
        val leftSleevePx = if (leftWrist != null) {
            hypot(leftWrist.position.x - leftShoulder.position.x, leftWrist.position.y - leftShoulder.position.y)
        } else 0f
        val rightSleevePx = if (rightWrist != null) {
            hypot(rightWrist.position.x - rightShoulder.position.x, rightWrist.position.y - rightShoulder.position.y)
        } else 0f
        val sleevePx = when {
            leftSleevePx > 0 && rightSleevePx > 0 -> (leftSleevePx + rightSleevePx) / 2f
            leftSleevePx > 0 -> leftSleevePx
            rightSleevePx > 0 -> rightSleevePx
            else -> torsoPx * 1.05f
        }
        val rawSleeve = sleevePx * pixelToCmRatio

        // 4. Leg length: hip to ankle
        val leftLegPx = if (leftAnkle != null) {
            hypot(leftAnkle.position.x - leftHip.position.x, leftAnkle.position.y - leftHip.position.y)
        } else 0f
        val rightLegPx = if (rightAnkle != null) {
            hypot(rightAnkle.position.x - rightHip.position.x, rightAnkle.position.y - rightHip.position.y)
        } else 0f
        val legPx = when {
            leftLegPx > 0 && rightLegPx > 0 -> (leftLegPx + rightLegPx) / 2f
            leftLegPx > 0 -> leftLegPx
            rightLegPx > 0 -> rightLegPx
            else -> torsoPx * 1.6f
        }
        val rawLeg = legPx * pixelToCmRatio

        // 5. Total height approximation (Head crown to ankle)
        val topY = if (nose != null) {
            // Crown is approximately 15% above nose
            val headOffset = (midShoulderY - nose.position.y).coerceAtLeast(15f)
            (nose.position.y - headOffset).coerceAtLeast(0f)
        } else {
            midShoulderY - torsoPx * 0.35f
        }
        val bottomY = when {
            leftAnkle != null && rightAnkle != null -> (leftAnkle.position.y + rightAnkle.position.y) / 2f
            leftAnkle != null -> leftAnkle.position.y
            rightAnkle != null -> rightAnkle.position.y
            else -> midHipY + torsoPx * 1.6f
        }
        val totalHeightPx = (bottomY - topY).coerceAtLeast(10f)
        val rawHeight = totalHeightPx * pixelToCmRatio

        // Tailor estimation for chest and waist circumference based on bi-acromial & bi-trochanteric widths
        val hipWidthPx = hypot(rightHip.position.x - leftHip.position.x, rightHip.position.y - leftHip.position.y)
        val rawChest = rawShoulder * 2.15
        val rawWaist = (hipWidthPx * pixelToCmRatio) * 2.25

        val smoothed = smoother.smooth(
            rawShoulder = rawShoulder,
            rawTorso = rawTorso,
            rawSleeve = rawSleeve,
            rawLeg = rawLeg,
            rawHeight = rawHeight,
            rawChest = rawChest,
            rawWaist = rawWaist
        )

        val rawDistances = mapOf(
            "shoulder_px" to shoulderPx,
            "torso_px" to torsoPx,
            "sleeve_px" to sleevePx,
            "leg_px" to legPx,
            "height_px" to totalHeightPx
        )

        return CalculationResult(
            measurements = smoothed.copy(
                landmarks = landmarksMap,
                rawDistancePx = rawDistances
            ),
            totalHeightPx = totalHeightPx
        )
    }

    /**
     * Calibrate pixel-to-cm ratio given the customer's known true height in cm.
     * Returns the calculated ratio (cm per pixel).
     */
    fun calibrateByHeight(knownHeightCm: Double, detectedHeightPx: Float): Double {
        if (detectedHeightPx <= 0f || knownHeightCm <= 0.0) return 0.15
        val ratio = knownHeightCm / detectedHeightPx
        return (Math.round(ratio * 1000.0) / 1000.0).coerceIn(0.01, 1.0)
    }
}
