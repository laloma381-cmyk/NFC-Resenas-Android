package com.nfcresenas.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc_history")
data class NfcHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val recordType: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
