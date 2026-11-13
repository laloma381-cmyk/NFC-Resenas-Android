package com.nfcresenas.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "",
    val address: String = "",
    val googleReviewUrl: String = "",
    val phone: String = "",
    val email: String = "",
    val instagramUrl: String = "",
    val facebookUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
