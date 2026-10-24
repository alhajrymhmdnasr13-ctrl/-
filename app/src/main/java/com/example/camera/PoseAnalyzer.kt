package com.example.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.model.BodyMeasurements
import com.example.model.MeasurementSmoother
import com.example.model.PoseMeasurementCalculator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PoseAnalyzer(
    var pixelToCmRatio: Double = 0.15,
    private val onMeasurementsCalculated: (BodyMeasurements, Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)
    private val smoother = MeasurementSmoother(historySize = 15)

    var isPaused: Boolean = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (isPaused) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        // Account for image coordinate rotation
        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val targetWidth = if (isRotated) imageProxy.height.toFloat() else imageProxy.width.toFloat()
        val targetHeight = if (isRotated) imageProxy.width.toFloat() else imageProxy.height.toFloat()

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val result = PoseMeasurementCalculator.calculate(
                    pose = pose,
                    imageWidth = targetWidth,
                    imageHeight = targetHeight,
                    pixelToCmRatio = pixelToCmRatio,
                    smoother = smoother
                )
                if (result != null) {
                    onMeasurementsCalculated(result.measurements, result.totalHeightPx)
                } else {
                    onMeasurementsCalculated(BodyMeasurements(isPoseDetected = false), 0f)
                }
            }
            .addOnFailureListener {
                onMeasurementsCalculated(BodyMeasurements(isPoseDetected = false), 0f)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun resetSmoothing() {
        smoother.reset()
    }

    fun close() {
        poseDetector.close()
    }
}
