package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.model.BodyMeasurements
import com.example.model.MeasurementSmoother
import com.example.model.PoseMeasurementCalculator
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object PhotoPoseProcessor {

    suspend fun processImageUri(
        context: Context,
        uri: Uri,
        pixelToCmRatio: Double
    ): PoseMeasurementCalculator.CalculationResult? = suspendCancellableCoroutine { cont ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()
            val detector = PoseDetection.getClient(options)

            detector.process(image)
                .addOnSuccessListener { pose ->
                    val smoother = MeasurementSmoother(1) // No moving average needed for single image
                    val result = PoseMeasurementCalculator.calculate(
                        pose = pose,
                        imageWidth = image.width.toFloat(),
                        imageHeight = image.height.toFloat(),
                        pixelToCmRatio = pixelToCmRatio,
                        smoother = smoother
                    )
                    detector.close()
                    cont.resume(result)
                }
                .addOnFailureListener {
                    detector.close()
                    cont.resume(null)
                }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }

    suspend fun processBitmap(
        bitmap: Bitmap,
        pixelToCmRatio: Double
    ): PoseMeasurementCalculator.CalculationResult? = suspendCancellableCoroutine { cont ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()
            val detector = PoseDetection.getClient(options)

            detector.process(image)
                .addOnSuccessListener { pose ->
                    val smoother = MeasurementSmoother(1)
                    val result = PoseMeasurementCalculator.calculate(
                        pose = pose,
                        imageWidth = bitmap.width.toFloat(),
                        imageHeight = bitmap.height.toFloat(),
                        pixelToCmRatio = pixelToCmRatio,
                        smoother = smoother
                    )
                    detector.close()
                    cont.resume(result)
                }
                .addOnFailureListener {
                    detector.close()
                    cont.resume(null)
                }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }
}
