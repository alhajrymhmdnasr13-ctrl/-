package com.example.model

data class Point2D(
    val x: Float, // Normalized 0..1
    val y: Float  // Normalized 0..1
)

data class BodyMeasurements(
    val shoulderCm: Double = 0.0,
    val torsoCm: Double = 0.0,
    val sleeveCm: Double = 0.0,
    val legCm: Double = 0.0,
    val heightCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val waistCm: Double = 0.0,
    val isPoseDetected: Boolean = false,
    val landmarks: Map<Int, Point2D> = emptyMap(),
    val rawDistancePx: Map<String, Float> = emptyMap()
)
