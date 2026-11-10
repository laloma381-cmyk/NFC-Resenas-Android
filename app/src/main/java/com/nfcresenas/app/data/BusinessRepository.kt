package com.nfcresenas.app.data

import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val dao: BusinessDao) {
    fun getAllBusinesses(): Flow<List<Business>> = dao.getAll()

    suspend fun saveBusiness(business: Business): Long = dao.insert(business)

    suspend fun updateBusiness(business: Business) = dao.update(business)

    suspend fun deleteBusiness(business: Business) = dao.delete(business)
}
