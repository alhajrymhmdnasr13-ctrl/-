package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val timestamp: String,
    val shoulder: Double,
    val torso: Double,
    val sleeve: Double,
    val leg: Double,
    val height: Double = 0.0,
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val garmentType: String = "Thobe / Suit",
    val notes: String = "",
    val unit: String = "cm"
)
