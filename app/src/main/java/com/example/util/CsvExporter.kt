package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.ClientEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    /**
     * Converts a list of clients into standard RFC-4180 compliant CSV format.
     */
    fun generateClientsCsv(clients: List<ClientEntity>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Client Name,Phone,Timestamp,Shoulder (cm),Torso (cm),Sleeve (cm),Leg (cm),Height (cm),Chest (cm),Waist (cm),Garment Type,Notes\n")

        for (c in clients) {
            val chestVal = if (c.chest > 0) c.chest else c.shoulder * 2.15
            val waistVal = if (c.waist > 0) c.waist else c.torso * 1.6
            val heightVal = if (c.height > 0) c.height else c.torso + c.leg + 35.0

            sb.append(c.id).append(",")
            sb.append(escapeCsv(c.name)).append(",")
            sb.append(escapeCsv(c.phone)).append(",")
            sb.append(escapeCsv(c.timestamp)).append(",")
            sb.append(String.format(Locale.US, "%.1f", c.shoulder)).append(",")
            sb.append(String.format(Locale.US, "%.1f", c.torso)).append(",")
            sb.append(String.format(Locale.US, "%.1f", c.sleeve)).append(",")
            sb.append(String.format(Locale.US, "%.1f", c.leg)).append(",")
            sb.append(String.format(Locale.US, "%.1f", heightVal)).append(",")
            sb.append(String.format(Locale.US, "%.1f", chestVal)).append(",")
            sb.append(String.format(Locale.US, "%.1f", waistVal)).append(",")
            sb.append(escapeCsv(c.garmentType)).append(",")
            sb.append(escapeCsv(c.notes)).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Writes CSV string directly to a content URI (e.g. from CreateDocument).
     */
    fun writeCsvToUri(context: Context, uri: Uri, clients: List<ClientEntity>): Boolean {
        return try {
            val csvContent = generateClientsCsv(clients)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Creates a temporary CSV file in cache and launches an ACTION_SEND chooser.
     */
    fun shareClientsCsv(context: Context, clients: List<ClientEntity>, title: String = "Export Clients CSV") {
        try {
            val csvContent = generateClientsCsv(clients)
            val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
            val filename = "tailor_clients_${sdf.format(Date())}.csv"
            val file = File(context.cacheDir, filename)

            FileOutputStream(file).use { fos ->
                fos.write(csvContent.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "AI Tailor Client Measurements CSV")
                putExtra(Intent.EXTRA_TEXT, "Attached is the export of tailor client measurements (${clients.size} records).")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
