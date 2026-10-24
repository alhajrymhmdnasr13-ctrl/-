package com.example

import com.example.data.ClientEntity
import com.example.model.MeasurementSmoother
import com.example.model.PoseMeasurementCalculator
import com.example.util.CsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testSmootherMovingAverage() {
    val smoother = MeasurementSmoother(historySize = 3)
    val m1 = smoother.smooth(40.0, 50.0, 60.0, 100.0, 170.0, 90.0, 80.0)
    assertEquals(40.0, m1.shoulderCm, 0.01)

    val m2 = smoother.smooth(42.0, 52.0, 62.0, 102.0, 172.0, 92.0, 82.0)
    assertEquals(41.0, m2.shoulderCm, 0.01)

    val m3 = smoother.smooth(44.0, 54.0, 64.0, 104.0, 174.0, 94.0, 84.0)
    assertEquals(42.0, m3.shoulderCm, 0.01)
  }

  @Test
  fun testCalibrationByHeight() {
    // 175cm customer spanning 1000 pixels in frame => 0.175 cm per pixel
    val ratio = PoseMeasurementCalculator.calibrateByHeight(175.0, 1000f)
    assertEquals(0.175, ratio, 0.001)
  }

  @Test
  fun testCsvExportFormatAndEscaping() {
    val client = ClientEntity(
      id = 1,
      name = "Al-Hajry, Mohammed",
      phone = "+966501234567",
      timestamp = "2026-10-23 15:30",
      shoulder = 46.5,
      torso = 53.0,
      sleeve = 63.5,
      leg = 104.0,
      height = 178.0,
      chest = 102.0,
      waist = 86.0,
      garmentType = "Thobe / Kandora",
      notes = "Japanese cotton, mandarin collar"
    )

    val csv = CsvExporter.generateClientsCsv(listOf(client))

    // Assert header presence
    assertTrue(csv.contains("ID,Client Name,Phone,Timestamp,Shoulder (cm)"))
    // Assert name with comma is escaped in quotes
    assertTrue(csv.contains("\"Al-Hajry, Mohammed\""))
    // Assert values are properly represented
    assertTrue(csv.contains("46.5,53.0,63.5,104.0"))
    assertTrue(csv.contains("Thobe / Kandora"))
  }
}
