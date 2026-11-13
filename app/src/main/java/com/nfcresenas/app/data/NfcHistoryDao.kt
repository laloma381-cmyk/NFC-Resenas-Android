package com.nfcresenas.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NfcHistoryDao {
    @Query("SELECT * FROM nfc_history ORDER BY createdAt DESC LIMIT 100")
    fun getRecent(): Flow<List<NfcHistory>>

    @Insert
    suspend fun insert(item: NfcHistory)

    @Query("DELETE FROM nfc_history")
    suspend fun clear()
}
