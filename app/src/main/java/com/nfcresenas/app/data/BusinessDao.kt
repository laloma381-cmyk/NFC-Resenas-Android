package com.nfcresenas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Business>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(business: Business): Long

    @Update
    suspend fun update(business: Business)

    @Delete
    suspend fun delete(business: Business)
}
