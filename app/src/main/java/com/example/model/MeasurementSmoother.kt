package com.example.model

class MeasurementSmoother(private val historySize: Int = 15) {
    private val shoulderHistory = ArrayDeque<Double>()
    private val torsoHistory = ArrayDeque<Double>()
    private val sleeveHistory = ArrayDeque<Double>()
    private val legHistory = ArrayDeque<Double>()
    private val heightHistory = ArrayDeque<Double>()
    private val chestHistory = ArrayDeque<Double>()
    private val waistHistory = ArrayDeque<Double>()

    @Synchronized
    fun smooth(
        rawShoulder: Double,
        rawTorso: Double,
        rawSleeve: Double,
        rawLeg: Double,
        rawHeight: Double,
        rawChest: Double,
        rawWaist: Double
    ): BodyMeasurements {
        val shoulder = pushAndAverage(shoulderHistory, rawShoulder)
        val torso = pushAndAverage(torsoHistory, rawTorso)
        val sleeve = pushAndAverage(sleeveHistory, rawSleeve)
        val leg = pushAndAverage(legHistory, rawLeg)
        val height = pushAndAverage(heightHistory, rawHeight)
        val chest = pushAndAverage(chestHistory, rawChest)
        val waist = pushAndAverage(waistHistory, rawWaist)

        return BodyMeasurements(
            shoulderCm = roundOneDecimal(shoulder),
            torsoCm = roundOneDecimal(torso),
            sleeveCm = roundOneDecimal(sleeve),
            legCm = roundOneDecimal(leg),
            heightCm = roundOneDecimal(height),
            chestCm = roundOneDecimal(chest),
            waistCm = roundOneDecimal(waist),
            isPoseDetected = true
        )
    }

    private fun pushAndAverage(queue: ArrayDeque<Double>, value: Double): Double {
        if (value <= 0.0) {
            return if (queue.isNotEmpty()) queue.average() else 0.0
        }
        if (queue.size >= historySize) {
            queue.removeFirst()
        }
        queue.addLast(value)
        return queue.average()
    }

    @Synchronized
    fun reset() {
        shoulderHistory.clear()
        torsoHistory.clear()
        sleeveHistory.clear()
        legHistory.clear()
        heightHistory.clear()
        chestHistory.clear()
        waistHistory.clear()
    }

    private fun roundOneDecimal(num: Double): Double {
        return Math.round(num * 10.0) / 10.0
    }
}
